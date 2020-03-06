package org.vaccineimpact.orderlyweb.security

import org.pac4j.core.config.Config
import org.pac4j.core.engine.DefaultSecurityLogic
import org.pac4j.core.engine.SecurityGrantedAccessAdapter
import org.pac4j.core.http.adapter.HttpActionAdapter
import org.pac4j.core.profile.CommonProfile
import org.pac4j.core.profile.ProfileManager
import org.pac4j.sparkjava.SparkWebContext
import org.vaccineimpact.orderlyweb.db.AuthorizationRepository
import org.vaccineimpact.orderlyweb.db.OrderlyAuthorizationRepository
import org.vaccineimpact.orderlyweb.models.permissions.PermissionSet
import org.vaccineimpact.orderlyweb.security.authorization.orderlyWebPermissions
import org.vaccineimpact.orderlyweb.security.clients.OrderlyWebIndirectClient
import java.util.*

class OrderlyWebSecurityLogic(private val authRepo: AuthorizationRepository = OrderlyAuthorizationRepository())
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
        updateProfile(context, config, clients)
        return super.perform(context,
                config,
                securityGrantedAccessAdapter,
                httpActionAdapter,
                clients, authorizers,
                matchers, inputMultiProfile, *parameters)
    }

    fun updateProfile(context: SparkWebContext?,
                      config: Config?,
                      clients: String?)
    {
        val client = clientFinder.find(config?.clients, context, clients).first()
        val manager = ProfileManager<CommonProfile>(context)
        val currentProfile = manager.get(true)

        if (currentProfile.isPresent && currentProfile.get().id != "anon")
        {
            // a user is logged in, so leave their profile as is
            return
        }

        if (client is OrderlyWebIndirectClient)
        {
            addOrUpdateAnonProfile(currentProfile, manager)
        }
        else
        {
            manager.remove(true)
        }
    }

    private fun addOrUpdateAnonProfile(currentProfile: Optional<CommonProfile>,
                                       profileManager: ProfileManager<CommonProfile>)
    {
        val permissions = PermissionSet(authRepo
                .getPermissionsForGroup("anon"))

        if (!currentProfile.isPresent)
        {
            val profile = CommonProfile().apply {
                id = "anon"
                orderlyWebPermissions = permissions
            }

            profileManager.save(true, profile, false)
        }
        else
        {
            currentProfile.get().orderlyWebPermissions = permissions
        }
    }
}
