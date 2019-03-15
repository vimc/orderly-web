package org.vaccineimpact.orderlyweb.security

import org.eclipse.egit.github.core.User
import org.eclipse.egit.github.core.client.GitHubClient
import org.eclipse.egit.github.core.client.RequestException
import org.eclipse.egit.github.core.service.UserService
import org.pac4j.core.context.WebContext
import org.pac4j.core.credentials.TokenCredentials
import org.pac4j.core.credentials.authenticator.Authenticator
import org.pac4j.core.exception.CredentialsException
import org.pac4j.core.profile.CommonProfile
import org.pac4j.core.util.CommonHelper
import org.vaccineimpact.orderlyweb.db.UserData


class GithubAuthenticator(private val userData: UserData,
                          private val githubApiClient: GitHubClient) : Authenticator<TokenCredentials>
{
    override fun validate(credentials: TokenCredentials?, context: WebContext)
    {
        if (credentials == null)
        {
            throw CredentialsException("No credentials supplied")
        }

        val token = credentials.token

        if (CommonHelper.isBlank(token))
        {
            throw CredentialsException("Token cannot be blank")
        }

        val email = validate(token)
        credentials.userProfile = CommonProfile().apply {
            this.addAttribute("url", "*")
            this.setId(email)
        }

    }

    private fun validate(token: String): String
    {
        githubApiClient.setOAuth2Token(token)

        val user: User = try
        {
            val service = UserService(githubApiClient)
            service.user
        }
        catch (e: RequestException)
        {
            if (e.status == 401)
            {
                throw CredentialsException(e.message)
            }
            else throw e
        }

        userData.addGithubUser(user.name, user.email)
        return user.email
    }
}
