package org.vaccineimpact.orderlyweb.security

import org.pac4j.core.config.Config
import org.pac4j.core.context.WebContext
import org.pac4j.core.context.session.SessionStore
import org.pac4j.core.engine.DefaultSecurityLogic
import org.pac4j.core.engine.SecurityGrantedAccessAdapter
import org.pac4j.core.http.adapter.HttpActionAdapter
import org.pac4j.sparkjava.SparkWebContext
import org.vaccineimpact.orderlyweb.security.authentication.AuthenticationConfig
import org.vaccineimpact.orderlyweb.security.authentication.OrderlyWebAuthenticationConfig

class OrderlyWebSecurityLogic(private val authenticationConfig: AuthenticationConfig = OrderlyWebAuthenticationConfig(),
                              private val guestUserManager: GuestUserManager = OrderlyWebGuestUserManager())
    : DefaultSecurityLogic()
{
    override fun perform(context: WebContext, sessionStore: SessionStore,
                         config: Config?, securityGrantedAccessAdapter: SecurityGrantedAccessAdapter?,
                         httpActionAdapter: HttpActionAdapter?, clients: String?, authorizers: String?,
                         matchers: String?, vararg parameters: Any?): Any?
    {
        guestUserManager.updateProfile(authenticationConfig.allowGuestUser,
                context as SparkWebContext,
                sessionStore,
                config,
                clients)

        return super.perform(context,
                sessionStore,
                config,
                securityGrantedAccessAdapter,
                httpActionAdapter,
                clients, authorizers,
                matchers,
                *parameters)
    }

}
