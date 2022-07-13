package org.vaccineimpact.orderlyweb.security.authentication

import org.pac4j.core.context.WebContext
import org.pac4j.core.context.session.SessionStore
import org.pac4j.core.credentials.Credentials
import org.pac4j.core.credentials.TokenCredentials
import org.pac4j.core.credentials.authenticator.Authenticator
import org.pac4j.core.exception.CredentialsException
import org.pac4j.core.profile.CommonProfile
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.Config
import org.vaccineimpact.orderlyweb.db.repositories.UserRepository
import org.vaccineimpact.orderlyweb.models.UserSource
import org.vaccineimpact.orderlyweb.security.providers.GithubApiClientAuthHelper
import org.vaccineimpact.orderlyweb.security.providers.GithubAuthHelper

class GithubAuthenticator(
        private val userRepository: UserRepository,
        private val appConfig: Config = AppConfig(),
        private val githubAuthHelper: GithubAuthHelper = GithubApiClientAuthHelper(appConfig)
) : Authenticator
{
    override fun validate(credentials: Credentials?, context: WebContext?, sessionStore: SessionStore?)
    {
        if (credentials == null)
        {
            throw CredentialsException("No credentials supplied")
        }

        val token = (credentials as TokenCredentials).token

        githubAuthHelper.authenticate(token)
        githubAuthHelper.checkGitHubOrgAndTeamMembership()

        val user = githubAuthHelper.getUser()
        val email = githubAuthHelper.getUserEmail()

        userRepository.addUser(email, user.login, user.name ?: "", UserSource.GitHub)

        credentials.userProfile = CommonProfile().apply {
            this.addAttribute("url", "*")
            this.setId(email)
        }
    }
}
