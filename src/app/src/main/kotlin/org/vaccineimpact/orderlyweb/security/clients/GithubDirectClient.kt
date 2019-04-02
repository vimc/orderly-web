package org.vaccineimpact.orderlyweb.security.clients

import org.eclipse.egit.github.core.client.GitHubClient
import org.pac4j.core.client.DirectClient
import org.pac4j.core.context.HttpConstants
import org.pac4j.core.credentials.TokenCredentials
import org.pac4j.core.credentials.extractor.HeaderExtractor
import org.pac4j.core.profile.CommonProfile
import org.vaccineimpact.orderlyweb.db.OrderlyUserRepository
import org.vaccineimpact.orderlyweb.models.ErrorInfo
import org.vaccineimpact.orderlyweb.security.authentication.GithubAuthenticator
import org.vaccineimpact.orderlyweb.db.OrderlyAuthorizationRepository

class GithubDirectClient : DirectClient<TokenCredentials, CommonProfile>(), OrderlyWebTokenCredentialClient
{
    override val errorInfo = ErrorInfo("github-token-invalid",
            "GitHub token not supplied in Authorization header, or GitHub token was invalid")

    override fun clientInit()
    {
        defaultCredentialsExtractor(HeaderExtractor(
                HttpConstants.AUTHORIZATION_HEADER,
                "token "))

        defaultAuthenticator(GithubAuthenticator(OrderlyUserRepository(), GitHubClient()))
        setAuthorizationGenerator(OrderlyAuthorizationRepository())
    }
}