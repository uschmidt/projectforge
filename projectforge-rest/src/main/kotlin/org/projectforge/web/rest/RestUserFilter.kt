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

package org.projectforge.web.rest

import mu.KotlinLogging
import org.projectforge.business.user.UserTokenType
import org.projectforge.login.LoginService
import org.projectforge.rest.Authentication
import org.projectforge.security.My2FARequestHandler
import org.projectforge.security.SecurityLogging
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import javax.servlet.http.HttpServletResponse

private val log = KotlinLogging.logger {}

/**
 * Filter ensures logged-in user. Is active for /rs (new Rest services) and /rest (old Rest services).
 */
class RestUserFilter : AbstractRestUserFilter(UserTokenType.REST_CLIENT) {
  @Autowired
  private lateinit var loginService: LoginService

  @Autowired
  private lateinit var my2FARequestHandler: My2FARequestHandler

  override fun authenticate(authInfo: RestAuthenticationInfo) {
    try {
      // Try to get the user by session id:
      loginService.checkLogin(authInfo.request, authInfo.response)?.let { userContext ->
        authInfo.user = userContext.user
        if (authInfo.success) {
          return
        }
      }
      restAuthenticationUtils.tokenAuthentication(authInfo, UserTokenType.REST_CLIENT, false)
      if (authInfo.success) {
        return
      }
      val requestURI = authInfo.request.requestURI
      // Don't log error for userStatus (used by React client for checking weather the user is logged in or not).
      if (requestURI == null || requestURI != "/rs/userStatus") {
        val msg =
          "Neither ${Authentication.AUTHENTICATION_USER_ID} nor ${Authentication.AUTHENTICATION_USERNAME}/${Authentication.AUTHENTICATION_TOKEN} is given for rest call: $requestURI. Rest call forbidden."
        log.error(msg)
        SecurityLogging.logSecurityWarn(authInfo.request, this::class.java, "REST AUTHENTICATION FAILED", msg)
      }
    } finally {
      if (authInfo.success) {
        if (!my2FARequestHandler.handleRequest(authInfo.request, authInfo.response)) {
          log.info { "2FA is required for this request: ${authInfo.request.requestURI}" }
          authInfo.resultCode = HttpStatus.PERMANENT_REDIRECT
        }
      }
    }
  }
}
