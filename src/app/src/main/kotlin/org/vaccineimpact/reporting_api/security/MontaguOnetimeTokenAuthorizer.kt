package org.vaccineimpact.reporting_api.security

import org.pac4j.core.authorization.authorizer.ProfileAuthorizer
import org.pac4j.core.context.WebContext
import org.pac4j.core.exception.CredentialsException
import org.pac4j.core.profile.CommonProfile

class MontaguOnetimeTokenAuthorizer
    : ProfileAuthorizer<CommonProfile>()
{
    override fun isProfileAuthorized(context: WebContext, profile: CommonProfile): Boolean
    {
        val requestedPath = context.path
        val claimedUrl = profile.getAttribute("url")

        if (requestedPath != claimedUrl)
            throw CredentialsException("Expected 'url' claim to be of type $requestedPath")

        return true

    }

    override fun isAuthorized(context: WebContext, profiles: List<CommonProfile>): Boolean
    {
        return isAnyAuthorized(context, profiles)
    }

}