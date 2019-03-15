package org.vaccineimpact.orderlyweb.security

import org.pac4j.core.client.DirectClient
import org.pac4j.core.context.HttpConstants
import org.pac4j.core.context.WebContext
import org.pac4j.core.credentials.TokenCredentials
import org.pac4j.core.credentials.extractor.HeaderExtractor
import org.pac4j.core.profile.CommonProfile
import org.vaccineimpact.orderlyweb.db.OrderlyUserData
import org.vaccineimpact.orderlyweb.models.ErrorInfo
import org.vaccineimpact.orderlyweb.models.permissions.PermissionSet

class GithubDirectClientWrapper : CredentialClientWrapper
{
    override val errorInfo: ErrorInfo = ErrorInfo("github-token-invalid", "GitHub token not supplied in Authorization header, or GitHub token was invalid")
    override val client = GithubDirectClient()
}

class GithubDirectClient : DirectClient<TokenCredentials, CommonProfile>()
{
    override fun clientInit(context: WebContext?)
    {
        defaultCredentialsExtractor(HeaderExtractor(
                HttpConstants.AUTHORIZATION_HEADER,
                "token ", this.name))

        defaultAuthenticator(GithubAuthenticator(OrderlyUserData()))

        setAuthorizationGenerator { _, profile -> addLoginPermission(profile) }
    }

    private fun addLoginPermission(profile: CommonProfile): CommonProfile
    {
        profile.montaguPermissions = PermissionSet("*/can-login")
        return profile
    }
}