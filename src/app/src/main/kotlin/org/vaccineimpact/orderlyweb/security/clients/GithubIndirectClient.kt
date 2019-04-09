package org.vaccineimpact.orderlyweb.security.clients

import org.pac4j.oauth.client.GitHubClient
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.OrderlyUserRepository
import org.vaccineimpact.orderlyweb.security.authentication.GithubOAuthAuthenticator
import org.vaccineimpact.orderlyweb.security.authentication.GithubOAuthProfileCreator

class GithubIndirectClient(key: String, secret: String): GitHubClient(key, secret) {
    init {
        setCallbackUrl("${AppConfig()["app.url"]}/login")
    }

    override fun clientInit()
    {
        defaultAuthenticator(GithubOAuthAuthenticator(configuration, this))

        super.clientInit()
        
        this.setProfileCreator(GithubOAuthProfileCreator(configuration, this, OrderlyUserRepository()))
    }
}