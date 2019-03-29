package org.vaccineimpact.orderlyweb.security.authentication

import org.eclipse.egit.github.core.User
import org.eclipse.egit.github.core.client.GitHubClient
import org.eclipse.egit.github.core.client.RequestException
import org.eclipse.egit.github.core.service.OrganizationService
import org.eclipse.egit.github.core.service.TeamService
import org.eclipse.egit.github.core.service.UserService
import org.pac4j.core.context.WebContext
import org.pac4j.core.credentials.TokenCredentials
import org.pac4j.core.credentials.authenticator.Authenticator
import org.pac4j.core.exception.CredentialsException
import org.pac4j.core.profile.CommonProfile
import org.pac4j.core.util.CommonHelper
import org.slf4j.LoggerFactory
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.Config
import org.vaccineimpact.orderlyweb.db.UserRepository
import org.vaccineimpact.orderlyweb.errors.BadConfigurationError
import org.vaccineimpact.orderlyweb.models.UserSource


class GithubAuthenticator(private val userRepository: UserRepository,
                          private val githubApiClient: GitHubClient,
                          private val appConfig: Config = AppConfig()) : Authenticator<TokenCredentials>
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

        val user = getGitHubUser()

        val githubOrg = appConfig["auth.github_org"]
        val teamName = appConfig["auth.github_team"]

        if (!githubOrg.isEmpty() && !userBelongToOrg(githubOrg, user))
        {
            throw CredentialsException("User is not a member of GitHub org $githubOrg or token does not include read:org scope")
        }
        if (!githubOrg.isEmpty() && !teamName.isEmpty() && !userBelongsToTeam(githubOrg, teamName, user))
        {
            throw CredentialsException("User is not a member of GitHub team $teamName")
        }

        // If the GitHub user has no public email set, we need to make an extra call to get it
        val email = user.email ?: getEmailForUser()

        userRepository.addUser(email, user.login, user.name ?: "", UserSource.GitHub)
        return email
    }

    private fun getEmailForUser(): String
    {
        return try
        {
            UserService(githubApiClient).emails.first()
        }
        catch (e: RequestException)
        {
            if (e.status == 404)
            {
                throw CredentialsException("GitHub token must include scope user:email")
            }
            else throw e
        }
    }

    private fun userBelongToOrg(githubOrg: String, user: User): Boolean
    {
        val organizationService = OrganizationService(githubApiClient)

        val members = try
        {
            organizationService.getMembers(githubOrg)
        }
        catch (e: RequestException)
        {
            if (e.status == 404)
            {
                throw BadConfigurationError("GitHub org $githubOrg does not exist")
            }
            else throw e
        }
        return members.map { it.login }.contains(user.login)
    }

    private fun userBelongsToTeam(githubOrg: String, teamName: String, user: User): Boolean
    {
        val teamService = TeamService(githubApiClient)
        val team = teamService
                .getTeams(githubOrg).firstOrNull {
                    it.name == teamName
                }
                ?: throw BadConfigurationError("GitHub org $githubOrg has no team called $teamName")

        val members = teamService.getMembers(team.id)
        return members.contains(user)
    }

    private fun getGitHubUser(): User
    {
        return try
        {
            val service = UserService(githubApiClient)
            service.user
        }
        catch (e: RequestException)
        {
            if (e.status == 401)
            {
                throw CredentialsException(e.message?:"")
            }
            else throw e
        }
    }

}