package org.vaccineimpact.orderlyweb.security

import org.pac4j.core.client.finder.DefaultSecurityClientFinder
import org.pac4j.core.config.Config
import org.pac4j.core.profile.CommonProfile
import org.pac4j.core.profile.ProfileManager
import org.pac4j.sparkjava.SparkWebContext
import org.vaccineimpact.orderlyweb.db.repositories.AuthorizationRepository
import org.vaccineimpact.orderlyweb.db.repositories.OrderlyAuthorizationRepository
import org.vaccineimpact.orderlyweb.models.permissions.PermissionSet
import org.vaccineimpact.orderlyweb.security.authorization.orderlyWebPermissions
import org.vaccineimpact.orderlyweb.security.clients.OrderlyWebIndirectClient
import java.util.*

interface GuestUserManager
{
    fun updateProfile(context: SparkWebContext?,
                      config: Config?,
                      clients: String?)
}

class OrderlyWebGuestUserManager(
        private val authRepo: AuthorizationRepository = OrderlyAuthorizationRepository()) : GuestUserManager
{
    companion object {
        const val GUEST_USER = "guest"
    }

    private val clientFinder = DefaultSecurityClientFinder()
    override fun updateProfile(context: SparkWebContext?,
                               config: Config?,
                               clients: String?)
    {
        val client = clientFinder.find(config?.clients, context, clients).first()
        val manager = ProfileManager<CommonProfile>(context)
        val currentProfile = manager.get(true)

        if (currentProfile.isPresent && currentProfile.get().id != "guest")
        {
            // a user is logged in, so leave their profile as is
            return
        }

        if (client is OrderlyWebIndirectClient)
        {
            addOrUpdateGuestProfile(currentProfile, manager)
        }
        else
        {
            manager.remove(true)
        }
    }

    private fun addOrUpdateGuestProfile(currentProfile: Optional<CommonProfile>,
                                       profileManager: ProfileManager<CommonProfile>)
    {
        val permissions = PermissionSet(authRepo.getPermissionsForGroup("guest"))

        if (!currentProfile.isPresent)
        {
            val profile = CommonProfile().apply {
                id = "guest"
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