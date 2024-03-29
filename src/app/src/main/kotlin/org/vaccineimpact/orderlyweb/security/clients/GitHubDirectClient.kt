package org.vaccineimpact.orderlyweb.security.clients

import org.pac4j.core.client.DirectClient
import org.pac4j.core.context.HttpConstants
import org.pac4j.core.credentials.extractor.HeaderExtractor
import org.vaccineimpact.orderlyweb.db.repositories.OrderlyUserRepository
import org.vaccineimpact.orderlyweb.models.ErrorInfo
import org.vaccineimpact.orderlyweb.security.authentication.GithubAuthenticator
import org.vaccineimpact.orderlyweb.security.authorization.OrderlyAuthorizationGenerator

class GitHubDirectClient : DirectClient(), OrderlyWebTokenCredentialClient
{
    override val errorInfo = ErrorInfo(
            "github-token-invalid",
            "GitHub token not supplied in Authorization header, or GitHub token was invalid"
    )

    override fun internalInit(forceInit: Boolean)
    {
        defaultCredentialsExtractor(
                HeaderExtractor(
                        HttpConstants.AUTHORIZATION_HEADER,
                        "token "
                )
        )

        defaultAuthenticator(GithubAuthenticator(OrderlyUserRepository()))
        setAuthorizationGenerator(OrderlyAuthorizationGenerator())
    }
}
