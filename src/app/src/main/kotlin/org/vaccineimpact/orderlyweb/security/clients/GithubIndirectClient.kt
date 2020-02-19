package org.vaccineimpact.orderlyweb.security.clients

import org.pac4j.core.context.WebContext
import org.pac4j.core.credentials.TokenCredentials
import org.pac4j.oauth.client.GitHubClient
import org.pac4j.oauth.credentials.OAuth20Credentials
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.OrderlyUserRepository
import org.vaccineimpact.orderlyweb.security.authentication.GithubOAuthAuthenticator
import org.vaccineimpact.orderlyweb.security.authentication.GithubOAuthProfileCreator
import org.vaccineimpact.orderlyweb.security.authorization.OrderlyAuthorizationGenerator

class GithubIndirectClient(key: String, secret: String): GitHubClient(key, secret) {
    init {
        setCallbackUrl("${AppConfig()["app.url"]}/login")
    }

    override fun clientInit()
    {
        scope = "read:user read:org user:email"
        defaultAuthenticator(GithubOAuthAuthenticator(configuration, this))
        super.clientInit()
        this.profileCreator = GithubOAuthProfileCreator(configuration, this, OrderlyUserRepository())
        setAuthorizationGenerator(OrderlyAuthorizationGenerator())
    }
}
