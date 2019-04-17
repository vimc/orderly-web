package org.vaccineimpact.orderlyweb.security.authorization

import org.pac4j.core.authorization.generator.AuthorizationGenerator
import org.pac4j.core.context.WebContext
import org.pac4j.core.profile.CommonProfile
import org.vaccineimpact.orderlyweb.db.AuthorizationRepository
import org.vaccineimpact.orderlyweb.db.OrderlyAuthorizationRepository

class OrderlyAuthorizationGenerator<T : CommonProfile>(private val authRepo: AuthorizationRepository = OrderlyAuthorizationRepository())
    : AuthorizationGenerator<T>
{
    override fun generate(context: WebContext?, profile: T): T
    {
        val permissions = authRepo.getPermissionsForUser(profile.id)
        profile.orderlyWebPermissions = permissions
        return profile
    }
}