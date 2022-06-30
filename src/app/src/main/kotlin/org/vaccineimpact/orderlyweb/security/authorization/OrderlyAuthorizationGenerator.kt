package org.vaccineimpact.orderlyweb.security.authorization

import org.pac4j.core.authorization.generator.AuthorizationGenerator
import org.pac4j.core.context.WebContext
import org.pac4j.core.context.session.SessionStore
import org.pac4j.core.profile.CommonProfile
import org.pac4j.core.profile.UserProfile
import org.vaccineimpact.orderlyweb.db.repositories.AuthorizationRepository
import org.vaccineimpact.orderlyweb.db.repositories.OrderlyAuthorizationRepository
import java.util.*

class OrderlyAuthorizationGenerator(private val authRepo: AuthorizationRepository = OrderlyAuthorizationRepository())
    : AuthorizationGenerator
{
    override fun generate(context: WebContext?, sessionStore: SessionStore, profile: UserProfile): Optional<UserProfile>
    {
        val permissions = authRepo.getPermissionsForUser(profile.id)
        (profile as CommonProfile).orderlyWebPermissions = permissions
        return Optional.of(profile)
    }
}