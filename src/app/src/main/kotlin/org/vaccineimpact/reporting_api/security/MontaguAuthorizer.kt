package org.vaccineimpact.reporting_api.security

import org.pac4j.core.authorization.authorizer.AbstractRequireAllAuthorizer
import org.pac4j.core.context.WebContext
import org.pac4j.core.profile.CommonProfile
import org.pac4j.sparkjava.SparkWebContext
import org.slf4j.LoggerFactory
import org.vaccineimpact.api.models.permissions.ReifiedPermission
import org.vaccineimpact.reporting_api.DirectActionContext

open class MontaguAuthorizer(requiredPermissions: Set<PermissionRequirement>)
    : AbstractRequireAllAuthorizer<PermissionRequirement, CommonProfile>()
{
    init
    {
        elements = requiredPermissions
    }

    private val logger = LoggerFactory.getLogger(MontaguAuthorizer::class.java)

    override fun isProfileAuthorized(context: WebContext, profile: CommonProfile): Boolean
    {
        val claimedUrl = profile.getAttribute("url")
        val requestedUrl = context.path

        if (claimedUrl != null && requestedUrl != claimedUrl)
        {
            logger.warn("This token is issued for $claimedUrl but the current request is for $requestedUrl")
            profile.addAttribute(MISSING_URL, "This token is issued for $claimedUrl but the current request is for $requestedUrl")
            return false
        }

        return super.isProfileAuthorized(context, profile)
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