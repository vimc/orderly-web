package org.vaccineimpact.orderlyweb.security

import org.pac4j.core.config.Config
import org.pac4j.core.engine.DefaultSecurityLogic
import org.pac4j.core.engine.SecurityGrantedAccessAdapter
import org.pac4j.core.http.adapter.HttpActionAdapter
import org.pac4j.sparkjava.SparkWebContext
import org.vaccineimpact.orderlyweb.security.authentication.AuthenticationConfig
import org.vaccineimpact.orderlyweb.security.authentication.OrderlyWebAuthenticationConfig

class OrderlyWebSecurityLogic(private val authenticationConfig: AuthenticationConfig = OrderlyWebAuthenticationConfig(),
                              private val anonUserManager: AnonUserManager = OrderlyWebAnonUserManager())
    : DefaultSecurityLogic<Any?, SparkWebContext>()
{
    override fun perform(context: SparkWebContext?,
                         config: Config?,
                         securityGrantedAccessAdapter: SecurityGrantedAccessAdapter<Any?, SparkWebContext?>?,
                         httpActionAdapter: HttpActionAdapter<Any?, SparkWebContext?>?,
                         clients: String?,
                         authorizers: String?,
                         matchers: String?,
                         inputMultiProfile: Boolean?,
                         vararg parameters: Any?): Any?
    {
        if (authenticationConfig.allowAnonUser)
        {
            anonUserManager.updateProfile(context, config, clients)
        }
        return super.perform(context,
                config,
                securityGrantedAccessAdapter,
                httpActionAdapter,
                clients, authorizers,
                matchers, inputMultiProfile, *parameters)
    }

}
