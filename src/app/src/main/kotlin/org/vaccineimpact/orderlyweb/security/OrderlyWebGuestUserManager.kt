package org.vaccineimpact.orderlyweb.security

import org.pac4j.core.client.finder.DefaultSecurityClientFinder
import org.pac4j.core.config.Config
import org.pac4j.core.context.session.SessionStore
import org.pac4j.core.profile.CommonProfile
import org.pac4j.core.profile.ProfileManager
import org.pac4j.sparkjava.SparkWebContext
import org.vaccineimpact.orderlyweb.db.repositories.AuthorizationRepository
import org.vaccineimpact.orderlyweb.db.repositories.OrderlyAuthorizationRepository
import org.vaccineimpact.orderlyweb.security.clients.OrderlyWebIndirectClient
import java.util.*

interface GuestUserManager
{
    fun updateProfile(
            allowGuestUser: Boolean,
            context: SparkWebContext,
            sessionStore: SessionStore,
            config: Config?,
            clients: String?
    )
}

class OrderlyWebGuestUserManager(
        private val authRepo: AuthorizationRepository = OrderlyAuthorizationRepository()
) : GuestUserManager
{
    companion object
    {
        const val GUEST_USER = "guest"
    }

    private val clientFinder = DefaultSecurityClientFinder()
    override fun updateProfile(
            allowGuestUser: Boolean,
            context: SparkWebContext,
            sessionStore: SessionStore,
            config: Config?,
            clients: String?
    )
    {
        val client = clientFinder.find(config?.clients, context, clients).first()
        val manager = ProfileManager(context, sessionStore)
        val currentProfile = manager.getProfile(CommonProfile::class.java)

        if (currentProfile.isPresent && currentProfile.get().id != GUEST_USER)
        {
            // a user is logged in, so leave their profile as is
            return
        }

        if (allowGuestUser && (client is OrderlyWebIndirectClient))
        {
            addOrUpdateGuestProfile(currentProfile, manager)
        }
        else
        {
            manager.removeProfiles()
        }
    }

    private fun addOrUpdateGuestProfile(
            currentProfile: Optional<CommonProfile>,
            profileManager: ProfileManager
    )
    {
        val permissions = authRepo.getPermissionsForGroup(GUEST_USER)
                .map { it.toString() }.toSet()

        if (!currentProfile.isPresent)
        {
            val profile = CommonProfile().apply {
                id = GUEST_USER
            }
            profile.permissions = permissions
            profileManager.save(true, profile, false)
        }
        else
        {
            currentProfile.get().permissions = permissions
        }
    }
}
