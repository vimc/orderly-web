package org.vaccineimpact.orderlyweb.security

import org.pac4j.core.config.Config
import org.pac4j.core.engine.DefaultSecurityLogic
import org.pac4j.core.engine.SecurityGrantedAccessAdapter
import org.pac4j.core.http.adapter.HttpActionAdapter
import org.pac4j.core.profile.CommonProfile
import org.pac4j.core.profile.ProfileManager
import org.pac4j.sparkjava.SparkWebContext
import org.vaccineimpact.orderlyweb.security.clients.GithubIndirectClient

class OrderlyWebSecurityLogic : DefaultSecurityLogic<Any?, SparkWebContext>()
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
        val client = clientFinder.find(config?.clients, context, clients).first()
        if (client is GithubIndirectClient)
        {
            // wipe anon profile
            ProfileManager<CommonProfile>(context).remove(true)
        }
        return super.perform(context,
                config,
                securityGrantedAccessAdapter,
                httpActionAdapter,
                clients, authorizers,
                matchers, inputMultiProfile, *parameters)
    }
}