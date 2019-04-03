package org.vaccineimpact.orderlyweb.security.authorization

import org.pac4j.core.authorization.authorizer.AbstractRequireAllAuthorizer
import org.pac4j.core.context.WebContext
import org.pac4j.core.profile.CommonProfile
import org.pac4j.sparkjava.SparkWebContext
import org.vaccineimpact.orderlyweb.DirectActionContext
import org.vaccineimpact.orderlyweb.models.PermissionRequirement

open class OrderlyWebAuthorizer(requiredPermissions: Set<PermissionRequirement>)
    : AbstractRequireAllAuthorizer<PermissionRequirement, CommonProfile>()
{
    init
    {
        elements = requiredPermissions
    }

    override fun check(context: WebContext, profile: CommonProfile, element: PermissionRequirement): Boolean
    {
        val profilePermissions = profile.orderlyWebPermissions
        val reifiedRequirement = element.reify(DirectActionContext(context as SparkWebContext))

        val hasPermission = profilePermissions.any { reifiedRequirement.satisfiedBy(it) }
        if (!hasPermission)
        {
            profile.missingPermissions.add(reifiedRequirement)
        }
        return hasPermission
    }
}