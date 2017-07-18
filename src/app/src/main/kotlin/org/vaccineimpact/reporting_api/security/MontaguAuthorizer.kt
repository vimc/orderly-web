package org.vaccineimpact.reporting_api.security

import org.pac4j.core.authorization.authorizer.AbstractRequireAllAuthorizer
import org.pac4j.core.context.WebContext
import org.pac4j.core.profile.CommonProfile
import org.pac4j.sparkjava.SparkWebContext
import org.vaccineimpact.api.models.permissions.ReifiedPermission
import org.vaccineimpact.reporting_api.DirectActionContext
import org.vaccineimpact.reporting_api.security.MISSING_PERMISSIONS
import org.vaccineimpact.reporting_api.security.PermissionRequirement

class MontaguAuthorizer(requiredPermissions: Set<PermissionRequirement>)
    : AbstractRequireAllAuthorizer<PermissionRequirement, CommonProfile>()
{
    init {
        elements = requiredPermissions
    }

    override fun check(context: WebContext, profile: CommonProfile, element: PermissionRequirement): Boolean
    {
        val profilePermissions = profile.montaguPermissions()
        val reifiedRequirement = element.reify(DirectActionContext(context as SparkWebContext))

        val hasPermission = profilePermissions.any { reifiedRequirement.satisfiedBy(it) }
        if (!hasPermission)
        {
            val missing = profile.getAttributeOrDefault(MISSING_PERMISSIONS, default = mutableSetOf<ReifiedPermission>())
            missing.add(reifiedRequirement)
        }
        return hasPermission
    }
}