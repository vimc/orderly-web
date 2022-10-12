package org.vaccineimpact.orderlyweb.security.clients

import org.pac4j.oauth.client.GitHubClient
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.repositories.OrderlyUserRepository
import org.vaccineimpact.orderlyweb.security.authentication.GithubOAuthAuthenticator
import org.vaccineimpact.orderlyweb.security.authentication.GithubOAuthProfileCreator
import org.vaccineimpact.orderlyweb.security.authorization.OrderlyAuthorizationGenerator

class GithubIndirectClient(key: String, secret: String) : GitHubClient(key, secret) {
    init {
        setCallbackUrl("${AppConfig()["app.url"]}/login")
    }

    override fun internalInit(forceReinit: Boolean)
    {
        scope = "read:org user:email"
        defaultAuthenticator(GithubOAuthAuthenticator(configuration, this))
        super.internalInit(forceReinit)
        this.profileCreator = GithubOAuthProfileCreator(configuration, this, OrderlyUserRepository())
        setAuthorizationGenerator(OrderlyAuthorizationGenerator())
    }
}
