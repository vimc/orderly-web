package org.vaccineimpact.orderlyweb.security.authentication

import com.github.scribejava.core.model.OAuth2AccessToken
import com.github.scribejava.core.model.Token
import org.pac4j.core.client.IndirectClient
import org.pac4j.oauth.config.OAuth20Configuration
import org.pac4j.oauth.profile.creator.OAuth20ProfileCreator
import org.pac4j.core.context.WebContext
import org.pac4j.core.profile.CommonProfile
import org.pac4j.core.profile.UserProfile
import org.pac4j.oauth.profile.github.GitHubProfile
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.Config
import org.vaccineimpact.orderlyweb.db.repositories.UserRepository
import org.vaccineimpact.orderlyweb.models.UserSource
import org.vaccineimpact.orderlyweb.security.providers.GithubApiClientAuthHelper
import org.vaccineimpact.orderlyweb.security.providers.GithubAuthHelper
import java.util.*

class GithubOAuthProfileCreator(config: OAuth20Configuration,
                                client: IndirectClient,
                                private val userRepository: UserRepository,
                                private val appConfig: Config = AppConfig(),
                                private val githubAuthHelper: GithubAuthHelper = GithubApiClientAuthHelper(appConfig))
    : OAuth20ProfileCreator(config, client)
{
    override fun retrieveUserProfileFromToken(context: WebContext, accessToken: Token): Optional<UserProfile>
    {
        val result = super.retrieveUserProfileFromToken(context, accessToken)

        githubAuthHelper.authenticate((accessToken as OAuth2AccessToken).accessToken)
        val user = githubAuthHelper.getUser()
        val email = githubAuthHelper.getUserEmail()

        //Id of the user profile is the email address
        (result.get() as CommonProfile).id = email

        //Add user to repo if it doesn't already exist
        userRepository.addUser(email, user.login, user.name ?: "", UserSource.GitHub)

        return result
    }
}