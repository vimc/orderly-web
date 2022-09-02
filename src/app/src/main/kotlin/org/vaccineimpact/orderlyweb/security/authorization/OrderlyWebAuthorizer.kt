package org.vaccineimpact.orderlyweb.security.authorization

import org.pac4j.core.authorization.authorizer.AbstractRequireAllAuthorizer
import org.pac4j.core.context.WebContext
import org.pac4j.core.context.session.SessionStore
import org.pac4j.core.profile.CommonProfile
import org.pac4j.core.profile.UserProfile
import org.pac4j.sparkjava.SparkWebContext
import org.vaccineimpact.orderlyweb.DirectActionContext
import org.vaccineimpact.orderlyweb.models.PermissionRequirement

open class OrderlyWebAuthorizer(
        requiredPermissions: Set<PermissionRequirement>
) : AbstractRequireAllAuthorizer<PermissionRequirement>()
{
    init
    {
        elements = requiredPermissions
    }

    override fun check(
            context: WebContext, sessionStore: SessionStore,
            profile: UserProfile, element: PermissionRequirement
    ): Boolean
    {
        val profilePermissions = (profile as CommonProfile).orderlyWebPermissions
        val reifiedRequirement = element.reify(DirectActionContext(context as SparkWebContext))

        val hasPermission = profilePermissions.any { reifiedRequirement.satisfiedBy(it) }
        if (!hasPermission)
        {
            profile.missingPermissions.add(reifiedRequirement)
        }
        return hasPermission
    }
}
