package org.vaccineimpact.orderlyweb.security.authorization

import org.pac4j.core.context.WebContext
import org.pac4j.core.profile.CommonProfile
import org.slf4j.LoggerFactory
import org.vaccineimpact.orderlyweb.models.PermissionRequirement

// The API Authorizer has to do additional checks that the claimed url matches the requested url, which are not
// required for Web endpoints
open class OrderlyWebAPIAuthorizer(requiredPermissions: Set<PermissionRequirement>)
    : OrderlyWebAuthorizer(requiredPermissions)
{
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
}