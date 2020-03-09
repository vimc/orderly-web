package org.vaccineimpact.orderlyweb.security

import org.pac4j.core.client.finder.DefaultSecurityClientFinder
import org.pac4j.core.config.Config
import org.pac4j.core.profile.CommonProfile
import org.pac4j.core.profile.ProfileManager
import org.pac4j.sparkjava.SparkWebContext
import org.slf4j.LoggerFactory
import org.vaccineimpact.orderlyweb.db.AuthorizationRepository
import org.vaccineimpact.orderlyweb.db.OrderlyAuthorizationRepository
import org.vaccineimpact.orderlyweb.models.permissions.PermissionSet
import org.vaccineimpact.orderlyweb.security.authorization.orderlyWebPermissions
import org.vaccineimpact.orderlyweb.security.clients.OrderlyWebIndirectClient
import java.util.*

interface AnonUserManager
{
    fun updateProfile(context: SparkWebContext?,
                      config: Config?,
                      clients: String?)
}

class OrderlyWebAnonUserManager(
        private val authRepo: AuthorizationRepository = OrderlyAuthorizationRepository()) : AnonUserManager
{
    private val clientFinder = DefaultSecurityClientFinder()
    val logger = LoggerFactory.getLogger("OrderlyWebAnonUserManager")
    override fun updateProfile(context: SparkWebContext?,
                               config: Config?,
                               clients: String?)
    {
        val client = clientFinder.find(config?.clients, context, clients).first()
        val manager = ProfileManager<CommonProfile>(context)
        val currentProfile = manager.get(true)

        if (currentProfile.isPresent && currentProfile.get().id != "anon")
        {
            logger.info("there is a user logged in")
            // a user is logged in, so leave their profile as is
            return
        }

        if (client is OrderlyWebIndirectClient)
        {
            logger.info("adding or updating anon profile")
            addOrUpdateAnonProfile(currentProfile, manager)
        }
        else
        {
            logger.info("external client, so removing anon profile")
            manager.remove(true)
        }
    }

    private fun addOrUpdateAnonProfile(currentProfile: Optional<CommonProfile>,
                                       profileManager: ProfileManager<CommonProfile>)
    {
        val permissions = PermissionSet(authRepo.getPermissionsForGroup("anon"))

        if (!currentProfile.isPresent)
        {
            logger.info("adding anon")
            val profile = CommonProfile().apply {
                id = "anon"
                orderlyWebPermissions = permissions
            }

            profileManager.save(true, profile, false)
        }
        else
        {
            logger.info("updating anon permissions")
            currentProfile.get().orderlyWebPermissions = permissions
        }
    }
}