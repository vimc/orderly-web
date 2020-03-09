package org.vaccineimpact.orderlyweb.security

import org.pac4j.core.config.Config
import org.pac4j.core.engine.DefaultSecurityLogic
import org.pac4j.core.engine.SecurityGrantedAccessAdapter
import org.pac4j.core.http.adapter.HttpActionAdapter
import org.pac4j.core.profile.CommonProfile
import org.pac4j.core.profile.ProfileManager
import org.pac4j.sparkjava.SparkWebContext
import org.slf4j.LoggerFactory
import org.vaccineimpact.orderlyweb.security.authentication.AuthenticationConfig
import org.vaccineimpact.orderlyweb.security.authentication.OrderlyWebAuthenticationConfig
import kotlin.math.log

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
        logger.info("OW security logic being called")
        if (authenticationConfig.allowAnonUser)
        {
            logger.info("anon user is enabled in config")
            anonUserManager.updateProfile(context, config, clients)
        }

        val client = clientFinder.find(config?.clients, context, clients).first()
        val manager = ProfileManager<CommonProfile>(context)
        val currentProfile = manager.get(true)
        if (currentProfile.isPresent)
        {
            logger.info("currentprofile: ${currentProfile.get().id}")
        }
        else {
            logger.info("no profile in context")
        }
        return super.perform(context,
                config,
                securityGrantedAccessAdapter,
                httpActionAdapter,
                clients, authorizers,
                matchers, inputMultiProfile, *parameters)
    }

}
