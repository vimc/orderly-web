package org.vaccineimpact.orderlyweb.security.authorization

import org.pac4j.core.authorization.authorizer.AbstractRequireAllAuthorizer
import org.pac4j.core.context.WebContext
import org.pac4j.core.profile.CommonProfile
import org.pac4j.sparkjava.SparkWebContext
import org.slf4j.LoggerFactory
import org.vaccineimpact.orderlyweb.DirectActionContext

open class OrderlyWebAuthorizer(requiredPermissions: Set<PermissionRequirement>)
    : AbstractRequireAllAuthorizer<PermissionRequirement, CommonProfile>()
{
    init
    {
        elements = requiredPermissions
    }

    private val logger = LoggerFactory.getLogger(OrderlyWebAuthorizer::class.java)

    override fun isProfileAuthorized(context: WebContext, profile: CommonProfile): Boolean
    {
        val claimedUrl = profile.getAttribute("url")
        var requestedUrl = context.path
        val queryParameters = context.requestParameters
                .filter { it.key != "access_token" }

        if (queryParameters.any())
        {
            requestedUrl = requestedUrl + "?" + queryParameters
                    .map { "${it.key}=${context.getRequestParameter(it.key)}" }
                    .joinToString("&")

        }

        if (claimedUrl == "*" || requestedUrl == claimedUrl)
        {
            return super.isProfileAuthorized(context, profile)
        }
        else
        {
            logger.warn("This token is issued for $claimedUrl but the current request is for $requestedUrl")
            profile.mismatchedURL = "This token is issued for $claimedUrl but the current request is for $requestedUrl"
            return false
        }

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