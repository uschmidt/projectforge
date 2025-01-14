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

package org.projectforge.caldav.config

import io.milton.servlet.MiltonFilter
import mu.KotlinLogging
import org.projectforge.business.user.UserAuthenticationsService
import org.projectforge.business.user.UserTokenType
import org.projectforge.caldav.service.SslSessionCache
import org.projectforge.rest.pub.CalendarSubscriptionServiceRest
import org.projectforge.rest.utils.RequestLog
import org.projectforge.security.SecurityLogging
import org.projectforge.web.rest.RestAuthenticationInfo
import org.projectforge.web.rest.RestAuthenticationUtils
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.context.WebApplicationContext
import org.springframework.web.context.support.WebApplicationContextUtils
import java.io.IOException
import javax.servlet.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

private val log = KotlinLogging.logger {}

/**
 * Ensuring a white url list for using Milton filter. MiltonFilter at default supports only black list.
 */
class PFMiltonFilter : MiltonFilter() {
    private lateinit var springContext: WebApplicationContext

    @Autowired
    private lateinit var restAuthenticationUtils: RestAuthenticationUtils

    @Autowired
    private lateinit var userAuthenticationsService: UserAuthenticationsService

    @Autowired
    private lateinit var sslSessionCache: SslSessionCache

    @Throws(ServletException::class)
    override fun init(filterConfig: FilterConfig) {
        super.init(filterConfig)
        springContext = WebApplicationContextUtils.getRequiredWebApplicationContext(filterConfig.servletContext)
        val beanFactory = springContext.autowireCapableBeanFactory
        beanFactory.autowireBean(this)
    }

    private fun authenticate(authInfo: RestAuthenticationInfo) {
        if (log.isDebugEnabled) {
            log.debug("Trying to authenticate user (requestUri=${RequestLog.asString(authInfo.request)})...")
        }
        val sslSessionUser = sslSessionCache.getSessionData(authInfo.request)
        if (sslSessionUser != null) {
            if (log.isDebugEnabled) {
                log.debug("User found by session id (requestUri=${RequestLog.asString(authInfo.request)})...")
            }
            authInfo.user = sslSessionUser
        } else {
            restAuthenticationUtils.basicAuthentication(authInfo, UserTokenType.DAV_TOKEN, true) { userString, authenticationToken ->
                val authenticatedUser = userAuthenticationsService.getUserByToken(authInfo.request, userString, UserTokenType.DAV_TOKEN, authenticationToken)
                if (authenticatedUser == null) {
                    val msg = "Can't authenticate user '$userString' by given token. User name and/or token invalid (requestUri=${RequestLog.asString(authInfo.request)}."
                    log.error(msg)
                    SecurityLogging.logSecurityWarn(authInfo.request, this::class.java, "${UserTokenType.DAV_TOKEN.name} AUTHENTICATION FAILED", msg)
                } else {
                    sslSessionCache.registerSessionData(authInfo.request, authenticatedUser)
                }
                authenticatedUser
            }
        }
    }

    @Throws(IOException::class, ServletException::class)
    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        request as HttpServletRequest
        if (!DAVMethodsInterceptor.handledByMiltonFilter(request)) {
            if (log.isDebugEnabled) {
                log.debug("Request is not for us (neither CalDAV nor CardDAV-call), processing normal filter chain (requestUri=${RequestLog.asString(request)})...")
            }
            // Not for us:
            chain.doFilter(request, response)
        } else {
            if (request.method == "PUT") {
                log.info { "DAV doesn't support PUT method (yet): ${request.requestURI}" }
                response as HttpServletResponse
                response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "PUT not (yet) supported by ProjectForge.")
                return
            }
            log.info("Request with method=${request.method} for Milton (requestUri=${RequestLog.asString(request)})...")
            restAuthenticationUtils.doFilter(request,
                    response,
                    UserTokenType.DAV_TOKEN,
                    authenticate = { authInfo -> authenticate(authInfo) },
                    doFilter = { -> super.doFilter(request, response, chain) }
            )
        }
    }
}
