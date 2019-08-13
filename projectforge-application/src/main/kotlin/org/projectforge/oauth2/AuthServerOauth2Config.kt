/////////////////////////////////////////////////////////////////////////////
//
// Project ProjectForge Community Edition
//         www.projectforge.org
//
// Copyright (C) 2001-2019 Micromata GmbH, Germany (www.micromata.com)
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

package org.projectforge.oauth2

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer
import org.springframework.security.oauth2.provider.token.RemoteTokenServices
import org.springframework.security.oauth2.provider.token.TokenStore
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore
import java.security.SecureRandom
import java.util.*


/**
 * https://www.baeldung.com/rest-api-spring-oauth2-angular
 */
@Configuration
@EnableAuthorizationServer
open class AuthServerOauth2Config : AuthorizationServerConfigurerAdapter() {
    private val log = org.slf4j.LoggerFactory.getLogger(AuthServerOauth2Config::class.java)

    @Value("\${security.oauth2.client.clientId}")
    private lateinit var clientId: String

    @Value("\${security.oauth2.client.clientSecret}")
    private lateinit var clientSecret: String

    @Value("\${security.oauth2.client.callbackUri}")
    private lateinit var callbackUri: String

    @Value("\${security.oauth2.client.tokenUri}")
    private lateinit var tokenUri: String

    @Autowired
    @Qualifier("authenticationManagerBean")
    private lateinit var authenticationManager: AuthenticationManager

    @Throws(Exception::class)
    override fun configure(oauthServer: AuthorizationServerSecurityConfigurer) {
        oauthServer
                .tokenKeyAccess("permitAll()")
                .checkTokenAccess("isAuthenticated()")
    }

    @Throws(Exception::class)
    override fun configure(clients: ClientDetailsServiceConfigurer) {
        val builder = clients
                .inMemory()
                .withClient(clientId)
                .authorizedGrantTypes("password", "authorization_code", "refresh_token")
                .scopes("read")
        if (clientSecret.isNullOrBlank() || clientSecret.length < 10) {
            log.error("Client secret must have at least 10 characters. Using random secret for preventing access.")
            builder.secret(generateRandomClientId())
        } else {
            builder.secret(clientSecret)
        }
        builder
                .redirectUris(callbackUri)
                .autoApprove(true)
    }

    @Throws(Exception::class)
    override fun configure(endpoints: AuthorizationServerEndpointsConfigurer) {
        endpoints
                .tokenStore(tokenStore())
                .authenticationManager(authenticationManager)
    }

    @Bean
    open fun tokenStore(): TokenStore {
        return InMemoryTokenStore()
    }

    @Primary
    @Bean
    open fun tokenService(): RemoteTokenServices {
        val tokenService = RemoteTokenServices()
        tokenService.setCheckTokenEndpointUrl(tokenUri)
        tokenService.setClientId(clientId)
        tokenService.setClientSecret(clientSecret)
        return tokenService
    }

    companion object {
        private fun generateRandomClientId(): String {
            val random = SecureRandom();
            val bytes = ByteArray(100)
            random.nextBytes(bytes);
            val encoder = Base64.getUrlEncoder().withoutPadding();
            return encoder.encodeToString(bytes);
        }

        @JvmStatic
        fun main(args: Array<String>) {
            println(generateRandomClientId())
            println(generateRandomClientId())
            println(generateRandomClientId())
        }
    }
}
