/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2022 Micromata GmbH, Germany (www.micromata.com)
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

package org.projectforge.rest.pub

import mu.KotlinLogging
import org.projectforge.business.user.UserDao
import org.projectforge.business.user.UserLocale
import org.projectforge.business.user.service.UserService
import org.projectforge.framework.i18n.I18nKeys
import org.projectforge.framework.i18n.translate
import org.projectforge.framework.persistence.user.api.UserContext
import org.projectforge.framework.persistence.user.entities.PFUserDO
import org.projectforge.framework.persistence.user.service.PasswordResetService
import org.projectforge.framework.time.TimeUnit
import org.projectforge.login.LoginService
import org.projectforge.rest.My2FAServicesRest
import org.projectforge.rest.My2FAType
import org.projectforge.rest.config.Rest
import org.projectforge.rest.config.RestUtils
import org.projectforge.rest.core.AbstractDynamicPageRest
import org.projectforge.rest.core.RestResolver
import org.projectforge.rest.dto.FormLayoutData
import org.projectforge.rest.dto.PostData
import org.projectforge.security.My2FAData
import org.projectforge.security.RegisterUser4Thread
import org.projectforge.security.SecurityLogging
import org.projectforge.ui.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.validation.Valid

private val log = KotlinLogging.logger {}

/**
 * This rest service should be available without login (public).
 * On this page, the user may enter his login name or e-mail and request a password reset.
 */
@RestController
@RequestMapping("${Rest.PUBLIC_URL}/passwordReset")
open class PasswordResetPageRest : AbstractDynamicPageRest() {
  @Autowired
  private lateinit var passwordResetService: PasswordResetService

  @Autowired
  private lateinit var my2FAServicesRest: My2FAServicesRest

  @Autowired
  private lateinit var userDao: UserDao

  @Autowired
  private lateinit var userService: UserService

  /**
   * For validating the Authenticator's OTP, or OTP sent by sms. The user must be assigned before by password reset token.
   * A 2FA is required first, before the password fields are shown.
   */
  @PostMapping("checkOTP")
  fun checkOTP(
    request: HttpServletRequest,
    response: HttpServletResponse,
    @Valid @RequestBody postData: PostData<My2FAData>,
    @RequestParam("redirect", required = false) redirect: String?
  ): ResponseEntity<*> {
    if (LoginService.getUserContext(request) != null) {
      return RestUtils.badRequest(translate(I18nKeys.ERROR_NOT_AVAILABLE_FOR_LOGGED_IN_USERS))
    }
    val result = securityCheck(request)
    result.badRequestResponseEntity?.let { return it }
    val user = result.data!!.user
    try {
      RegisterUser4Thread.registerUser(user)
      val otpCheckReslt = my2FAServicesRest.checkOTP(request, response, postData, redirect)
      if (otpCheckReslt.body?.targetType == TargetType.UPDATE) {
        // Update also the ui of the client (on success, the password fields will be shown after 2FA).
        otpCheckReslt.body.let {
          it.addVariable("ui", getLayout(request))
          val data = PasswordResetData()
          data.username = user.username
          it.addVariable("data", data)
        }
      }
      return otpCheckReslt
    } finally {
      RegisterUser4Thread.unregister()
    }
  }

  /**
   * Sends a OTP as code (text to mobile phone of user assigned to password reset session).
   */
  @GetMapping("sendSmsCode")
  fun sendSmsCode(request: HttpServletRequest): ResponseEntity<*> {
    if (LoginService.getUserContext(request) != null) {
      return RestUtils.badRequest(translate(I18nKeys.ERROR_NOT_AVAILABLE_FOR_LOGGED_IN_USERS))
    }
    val result = securityCheck(request, My2FAType.SMS)
    result.badRequestResponseEntity?.let { return it }
    try {
      RegisterUser4Thread.registerUser(result.data!!.user)
      return my2FAServicesRest.sendSmsCode(request)
    } finally {
      RegisterUser4Thread.unregister()
    }
  }

  /**
   * Cancel the password reset process (clears the user's session).
   */
  @GetMapping("cancel")
  fun cancel(request: HttpServletRequest): ResponseAction {
    getSessionData(request, false)?.let {
      // Delete token of current password reset session. The link of the e-mail will be invalid now.
      passwordResetService.deleteToken(it.token)
    }
    request.getSession(false)?.invalidate()
    return RestUtils.getRedirectToDefaultPageAction()
  }

  /**
   * @param token The token sent by mail (is mandatory for getting and checking the user).
   * @see [PasswordResetService.checkToken]
   */
  @GetMapping("dynamic")
  fun getForm(request: HttpServletRequest, @RequestParam("token") token: String): FormLayoutData {
    if (LoginService.getUserContext(request) != null) {
      return LayoutUtils.getMessageFormLayoutData(
        LAYOUT_TITLE,
        I18nKeys.ERROR_NOT_AVAILABLE_FOR_LOGGED_IN_USERS,
        UIColor.WARNING
      )
    }
    passwordResetService.checkToken(token)?.let { user ->
      request.getSession(true).setAttribute(SESSION_ATTRIBUTE_DATA, SessionData(token, user))
      UserLocale.registerLocale(request, user)
    }
    val layout = getLayout(request)
    return FormLayoutData(null, layout, createServerData(request))
  }

  @PostMapping
  fun post(request: HttpServletRequest, @RequestBody postData: PostData<PasswordResetData>)
      : ResponseEntity<*> {
    if (LoginService.getUserContext(request) != null) {
      return RestUtils.badRequest(translate(I18nKeys.ERROR_NOT_AVAILABLE_FOR_LOGGED_IN_USERS))
    }
    val result = securityCheck(request)
    result.badRequestResponseEntity?.let { return it }
    val user = result.data!!.user
    validateCsrfToken(request, postData)?.let { return it }
    val newPassword = postData.data.newPassword
    val newPasswordRepeat = postData.data.newPasswordRepeat

    if (!Arrays.equals(newPassword, newPasswordRepeat)) {
      val validationErrors = listOf(ValidationError.create("user.error.passwordAndRepeatDoesNotMatch"))
      return ResponseEntity(ResponseAction(validationErrors = validationErrors), HttpStatus.NOT_ACCEPTABLE)
    }
    log.info { "The user wants to change his password." }
    val errorMsgKeys = userService.internalChangePasswordAfter2FA(user.id, newPassword)
    processErrorKeys(errorMsgKeys)?.let {
      return it // Error messages occured:
    }
    cancel(request) // Clear session
    val layout =
      LayoutUtils.getMessageLayout(LAYOUT_TITLE, "user.changePassword.msg.passwordSuccessfullyChanged", UIColor.SUCCESS)
    layout.add(
      UIButton(
        "back",
        translate("back"),
        UIColor.DANGER,
      ).redirectToDefaultPage()
    )
    return ResponseEntity.ok(
      ResponseAction(
        targetType = TargetType.UPDATE,
        variables = mutableMapOf<String, Any>("ui" to layout)
      )
    )
  }

  private fun getLayout(request: HttpServletRequest): UILayout {
    // Session required for data, csrf and 2FA handling.
    val data = getSessionData(request, true)
    val user = data?.user
    val layout = UILayout(LAYOUT_TITLE)
    val lastSuccessful2FA = My2FAServicesRest.getLastSuccessful2FAFromSession(request)
    // has successful 2FA, not older than 10 minutes:
    val hasSuccessful2FA =
      lastSuccessful2FA != null && System.currentTimeMillis() - lastSuccessful2FA < 10 * TimeUnit.MINUTE.millis
    if (user != null && !hasSuccessful2FA) {
      // User given, but first 2FA required:
      my2FAServicesRest.fillLayout4PublicPage(layout, UserContext(user), this::class.java, mailOTPDisabled = true)
      LayoutUtils.process(layout)
      return layout
    }

    if (user == null) {
      // Session not found: show error only:
      layout.add(
        UIAlert(
          message = "password.reset.error",
          color = UIColor.DANGER
        )
      )
    } else {
      // Successful 2FA, show password fields:
      layout
        .add(
          UIReadOnlyField("username", label = "username")
        )
        .add(
          UIInput(
            "newPassword",
            label = "user.changePassword.newPassword",
            dataType = UIDataType.PASSWORD,
            required = true
          )
        )
        .add(
          UIInput(
            "newPasswordRepeat",
            label = "passwordRepeat",
            dataType = UIDataType.PASSWORD,
            required = true
          )
        )

    }
    layout.add(
      UIButton(
        "cancel",
        translate("cancel"),
        UIColor.DANGER,
        responseAction = ResponseAction(
          RestResolver.getRestUrl(this::class.java, "cancel"),
          targetType = TargetType.GET
        ),
      )
    )
    if (user != null) {
      layout.add(
        UIButton(
          "update",
          translate("update"),
          UIColor.SUCCESS,
          responseAction = ResponseAction(RestResolver.getRestUrl(this::class.java), targetType = TargetType.POST),
          default = true
        )
      )
    }
    LayoutUtils.process(layout)
    return layout
  }

  class PasswordResetData : My2FAData() {
    var username: String? = null
    var newPassword: CharArray? = null
    var newPasswordRepeat: CharArray? = null
  }

  /**
   * User must be pre-logged-in by password (UserContext must be given in user's http session), or
   * user must be registered via [registerUserForPublic2FA].
   * @param type OTP per mail is not allowed for non-context-users (especially for password reset).
   */
  private fun securityCheck(request: HttpServletRequest, type: My2FAType? = null): SecurityCheckResult {
    val data = getSessionData(request)
    data?.let { data ->
      return SecurityCheckResult(data)
    }
    SecurityLogging.logSecurityWarn(
      request,
      this::class.java,
      "No password reset user tried to do call checkOTP (denied)"
    )
    return SecurityCheckResult(badRequestResponseEntity = ResponseEntity<Any>(HttpStatus.BAD_REQUEST))
  }

  private fun getSessionData(request: HttpServletRequest, createSession: Boolean = false): SessionData? {
    return request.getSession(createSession).getAttribute(SESSION_ATTRIBUTE_DATA) as? SessionData
  }

  private class SecurityCheckResult(
    val data: SessionData? = null,
    val badRequestResponseEntity: ResponseEntity<*>? = null
  )

  private class SessionData(var token: String, var user: PFUserDO)

  companion object {
    private const val SESSION_ATTRIBUTE_DATA = "passwordReset.data"
    private const val LAYOUT_TITLE = "password.reset.title"
  }
}
