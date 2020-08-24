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
import org.projectforge.framework.i18n.translate
import org.projectforge.framework.persistence.user.api.ThreadLocalUserContext
import org.projectforge.plugins.core.AbstractPlugin
import org.projectforge.plugins.core.PluginAdminService
import org.projectforge.rest.config.Rest
import org.projectforge.rest.core.AbstractDynamicPageRest
import org.projectforge.rest.core.PagesResolver
import org.projectforge.rest.core.RestResolver
import org.projectforge.rest.dto.FormLayoutData
import org.projectforge.rest.dto.PostData
import org.projectforge.ui.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest

private val log = KotlinLogging.logger {}

@RestController
@RequestMapping("${Rest.URL}/plugins")
class PluginPageRest : AbstractDynamicPageRest() {

    @Autowired
    private val pluginAdminService: PluginAdminService? = null

    class PluginData(
            var userId: Int? = null,
            var plugin: List<AbstractPlugin>? = null
    )

    @PostMapping
    fun togglePlugin(request: HttpServletRequest, @RequestBody postData: PostData<PluginData>)
            : ResponseEntity<ResponseAction>? {
        println()
        //pluginAdminService!!.storePluginToBeActivated(plugin.info.id, !isActivated(activatedPlugins, plugin))
        return ResponseEntity(ResponseAction("dynamic",
                targetType = TargetType.REDIRECT
        ), HttpStatus.OK)
    }

    @GetMapping("dynamic")
    fun getForm(request: HttpServletRequest): FormLayoutData {
        val userId = ThreadLocalUserContext.getUserId()

        val availables = pluginAdminService!!.availablePlugins
        val activatedPlugins = pluginAdminService.activatedPluginsFromConfiguration

        val data = PluginData(userId, availables)

        val layout = UILayout("system.pluginAdmin.title")

        for (plugin in availables) {
            val row = UIRow()
            val idCol = UICol()
            val descCol = UICol()
            val buttonCol = UICol()
            row.add(idCol)
            row.add(descCol)
            row.add(buttonCol)

            idCol.add(UILabel(plugin.info.id))

            descCol.add(UILabel(plugin.info.description))

            val variables: MutableMap<String, Any>? = HashMap()
            variables!!["pluginId"] = plugin.info.id
            variables["activated"] = !isActivated(activatedPlugins, plugin)

            val action = ResponseAction(
                    RestResolver.getRestUrl(this::class.java),
                    targetType = TargetType.POST,
                    variables = variables
            )

            var button: UIButton?
            if (isActivated(activatedPlugins, plugin)) {
                button = UIButton("deactivate",
                        title = translate("system.pluginAdmin.button.deactivate"),
                        color = UIColor.SUCCESS,
                        responseAction = action)
            } else {
                button = UIButton("activate",
                        title = translate("system.pluginAdmin.button.activate"),
                        color = UIColor.DANGER,
                        responseAction = action)
            }
            buttonCol.add(button)
            layout.add(row)
        }

        LayoutUtils.process(layout)
        return FormLayoutData(data, layout, createServerData(request))
    }

    private fun isActivated(activatedPlugins: List<String>, plugin: AbstractPlugin): Boolean {
        return activatedPlugins.contains(plugin.info.id)
    }
}
