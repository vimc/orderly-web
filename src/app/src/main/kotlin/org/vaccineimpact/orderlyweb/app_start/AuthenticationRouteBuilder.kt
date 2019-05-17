package org.vaccineimpact.orderlyweb.app_start

import org.pac4j.sparkjava.CallbackRoute
import org.pac4j.sparkjava.LogoutRoute
import org.vaccineimpact.orderlyweb.security.WebSecurityConfigFactory
import org.vaccineimpact.orderlyweb.security.authentication.AuthenticationConfig
import org.vaccineimpact.orderlyweb.security.authentication.AuthenticationProvider

interface AuthenticationRouteBuilder
{
    fun logout(): LogoutRoute
    fun loginCallback(): CallbackRoute
}

class OrderlyAuthenticationRouteBuilder(private val authenticationConfig: AuthenticationConfig)
    : AuthenticationRouteBuilder
{
    private val client = authenticationConfig.getAuthenticationIndirectClient()
    private val securityConfig = WebSecurityConfigFactory(client, setOf())
            .build()

    override fun logout(): LogoutRoute
    {
        val synchroniseLogout = authenticationConfig.getConfiguredProvider() == AuthenticationProvider.Montagu
        return LogoutRoute(securityConfig)
                .apply {
                    destroySession = true
                    defaultUrl = "/"
                    centralLogout = synchroniseLogout
                }
    }

    override fun loginCallback(): CallbackRoute
    {
        return CallbackRoute(securityConfig)
    }
}
