package org.vaccineimpact.orderlyweb.security

import org.pac4j.core.client.DirectClient
import org.pac4j.core.context.HttpConstants
import org.pac4j.core.context.WebContext
import org.pac4j.core.credentials.TokenCredentials
import org.pac4j.core.credentials.extractor.HeaderExtractor
import org.pac4j.core.profile.CommonProfile
import org.vaccineimpact.orderlyweb.models.ErrorInfo
import org.vaccineimpact.orderlyweb.models.permissions.PermissionSet

class GithubDirectClient : DirectClient<TokenCredentials, CommonProfile>(), OrderlyWebCredentialClient
{
    override val errorInfo = ErrorInfo("github-token-invalid",
            "GitHub token not supplied in Authorization header, or GitHub token is invalid")

    override fun clientInit(context: WebContext?)
    {
        defaultCredentialsExtractor(HeaderExtractor(
                HttpConstants.AUTHORIZATION_HEADER,
                "token ", this.name))

        defaultAuthenticator(GithubAuthenticator())

        setAuthorizationGenerator { _, profile -> addLoginPermission(profile) }
    }

    private fun addLoginPermission(profile: CommonProfile): CommonProfile
    {
        profile.montaguPermissions = PermissionSet("*/can-login")
        return profile
    }
}