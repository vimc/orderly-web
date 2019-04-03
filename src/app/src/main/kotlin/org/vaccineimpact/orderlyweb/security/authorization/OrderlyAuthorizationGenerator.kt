package org.vaccineimpact.orderlyweb.security.authorization

import org.pac4j.core.authorization.generator.AuthorizationGenerator
import org.pac4j.core.context.WebContext
import org.pac4j.core.profile.CommonProfile
import org.vaccineimpact.orderlyweb.db.AuthorizationRepository
import org.vaccineimpact.orderlyweb.db.OrderlyAuthorizationRepository

class OrderlyAuthorizationGenerator(private val authRepo: AuthorizationRepository = OrderlyAuthorizationRepository())
    : AuthorizationGenerator<CommonProfile>
{
    override fun generate(context: WebContext?, profile: CommonProfile): CommonProfile
    {
        val permissions = authRepo.getPermissionsForUser(profile.id)
        profile.orderlyWebPermissions = permissions
        return profile
    }
}