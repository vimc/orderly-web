package org.vaccineimpact.orderlyweb.security.authentication

import com.github.scribejava.core.model.OAuth2AccessToken
import org.pac4j.core.client.IndirectClient
import org.pac4j.oauth.config.OAuth20Configuration
import org.pac4j.oauth.profile.creator.OAuth20ProfileCreator
import org.pac4j.core.context.WebContext
import org.pac4j.oauth.profile.github.GitHubProfile
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.Config
import org.vaccineimpact.orderlyweb.db.UserRepository
import org.vaccineimpact.orderlyweb.models.UserSource
import org.vaccineimpact.orderlyweb.security.providers.GithubApiClientAuthHelper
import org.vaccineimpact.orderlyweb.security.providers.GithubAuthHelper

class GithubOAuthProfileCreator(config: OAuth20Configuration,
                                client: IndirectClient<*, *>,
                                private val userRepository: UserRepository,
                                private val appConfig: Config = AppConfig(),
                                private val githubAuthHelper: GithubAuthHelper = GithubApiClientAuthHelper(appConfig))
    : OAuth20ProfileCreator<GitHubProfile>(config, client)
{
    override fun retrieveUserProfileFromToken(context: WebContext, accessToken: OAuth2AccessToken) : GitHubProfile
    {
        var result = super.retrieveUserProfileFromToken(context, accessToken)

        //Make credentials consistent with those produced by MontaguAuthenticator
        val user = githubAuthHelper.getUser()
        val email = githubAuthHelper.getUserEmail()

        result.id = email

        //Add user to repo if it doesn't already exist
        userRepository.addUser(email, user.login, user.name ?: "", UserSource.GitHub)

        return result
    }
}