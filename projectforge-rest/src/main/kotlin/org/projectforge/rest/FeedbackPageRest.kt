/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2020 Micromata GmbH, Germany (www.micromata.com)
//
// ProjectForge is dual-licensed.
//
// This community edition is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License as published
// by the Free Software Foundation; version 3 of the License.
//
// This community edition is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
// Public License for more details.
//
// You should have received a copy of the GNU General Public License along
// with this program; if not, see http://www.gnu.org/licenses/.
//
/////////////////////////////////////////////////////////////////////////////

package org.projectforge.rest

import mu.KotlinLogging
import org.projectforge.framework.i18n.I18nHelper
import org.projectforge.framework.i18n.translate
import org.projectforge.framework.persistence.user.api.ThreadLocalUserContext
import org.projectforge.mail.Mail
import org.projectforge.mail.SendMail
import org.projectforge.rest.config.Rest
import org.projectforge.rest.core.AbstractDynamicPageRest
import org.projectforge.rest.core.PagesResolver
import org.projectforge.rest.core.RestResolver
import org.projectforge.rest.dto.FormLayoutData
import org.projectforge.rest.dto.PostData
import org.projectforge.ui.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.servlet.http.HttpServletRequest

private val log = KotlinLogging.logger {}

@RestController
@RequestMapping("${Rest.URL}/feedback")
class FeedbackPageRest : AbstractDynamicPageRest() {

    class FeedbackData(
            var userId: Int? = null,
            var receiver: String? = null,
            var sender: String? = null,
            var subject: String? = null,
            var description: String? = null
    )

    @PostMapping
    fun send(request: HttpServletRequest, @RequestBody postData: PostData<FeedbackData>)
            : ResponseEntity<ResponseAction>? {
        validateCsrfToken(request, postData)?.let { return it }
        log.info("Send feedback.")
        var result: Boolean
        try {
            result = sendFeedback(postData.data)
        } catch (ex: Throwable) {
            log.error(ex.message, ex)
            result = false
        }

        if (result) {
            return ResponseEntity(ResponseAction(PagesResolver.getDefaultUrl(),
                    message = ResponseAction.Message("feedback.mailSendSuccessful"),
                    targetType = TargetType.REDIRECT
            ), HttpStatus.OK)
        } else {
            return ResponseEntity(ResponseAction("dynamic",
                    message = ResponseAction.Message("mail.error.exception"),
                    targetType = TargetType.GET
            ), HttpStatus.BAD_REQUEST)
        }
    }

    fun sendFeedback(data: FeedbackData): Boolean {
        if (data.sender == null) {
            data.sender = ThreadLocalUserContext.getUser()!!.getFullname()
        }
        val params = HashMap<String, Any?>()

        params["data"] = data
        val msg = Mail()
        msg.addTo(data.receiver)
        msg.setProjectForgeSubject(data.subject)
        params["subject"] = data.subject

        val sendMail = SendMail()

        val content = sendMail.renderGroovyTemplate(msg,
                "mail/feedback.txt",
                params,
                I18nHelper.getLocalizedMessage("administration.configuration.param.feedbackEMail.label"),
                ThreadLocalUserContext.getUser())
        msg.content = content
        msg.contentType = Mail.CONTENTTYPE_TEXT
        return sendMail.send(msg, null, null)
    }

    @GetMapping("dynamic")
    fun getForm(request: HttpServletRequest): FormLayoutData {
        val userId = ThreadLocalUserContext.getUserId()
        val data = FeedbackData(userId)

        val layout = UILayout("feedback.send.title")

        layout.add(createRow("feedback.receiver", " admin@dev-null.com"))
                .add(createRow("feedback.sender", ThreadLocalUserContext.getUser().username!!))
                .add(UITextArea("description", label = "description"))
                .addAction(UIButton("cancel",
                        translate("cancel"),
                        UIColor.DANGER,
                        responseAction = ResponseAction(PagesResolver.getDefaultUrl(), targetType = TargetType.REDIRECT))
                )
                .addAction(UIButton("pacman",
                        translate("menu.pacman"),
                        UIColor.WARNING//,
                        //responseAction = ResponseAction(RestResolver.getRestUrl(this::class.java), targetType = TargetType.POST)
                        )
                )
                .addAction(UIButton("send",
                        translate("send"),
                        UIColor.SUCCESS,
                        responseAction = ResponseAction(RestResolver.getRestUrl(this::class.java), targetType = TargetType.POST),
                        default = true)
                )
        LayoutUtils.process(layout)
        return FormLayoutData(null, layout, createServerData(request))
    }

    private fun createRow(label: String, value: String): UIRow {
        return UIRow()
                .add(UICol(UILength(12, 6, 6, 4, 3))
                        .add(UILabel(label)))
                .add(UICol(UILength(12, 6, 6, 8, 9))
                        .add(UILabel("'$value")))
    }
}
