package org.vaccineimpact.orderlyweb.security.authentication

import org.pac4j.oauth.config.OAuth20Configuration
import org.pac4j.oauth.credentials.authenticator.OAuth20Authenticator
import org.pac4j.core.client.IndirectClient
import org.pac4j.core.context.WebContext
import org.pac4j.oauth.credentials.OAuth20Credentials
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.Config
import org.vaccineimpact.orderlyweb.security.providers.GithubApiClientAuthHelper
import org.vaccineimpact.orderlyweb.security.providers.GithubAuthHelper


class GithubOAuthAuthenticator(config: OAuth20Configuration,
                               client: IndirectClient<*,*>,
                               private val appConfig: Config = AppConfig(),
                               private val githubAuthHelper: GithubAuthHelper = GithubApiClientAuthHelper(appConfig))
    : OAuth20Authenticator(config, client) {

    override fun validate(credentials: OAuth20Credentials, context: WebContext?) {
        super.validate(credentials, context)

        val token = credentials.accessToken.accessToken

        //Check github user is member of org/team allowed to access OrderlyWeb
        githubAuthHelper.authenticate(token)
        githubAuthHelper.checkGitHubOrgAndTeamMembership()

    }

}