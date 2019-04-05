package org.vaccineimpact.orderlyweb.security.providers

import org.eclipse.egit.github.core.User
import org.pac4j.core.exception.CredentialsException
import org.pac4j.core.util.CommonHelper
import org.eclipse.egit.github.core.client.GitHubClient
import org.eclipse.egit.github.core.client.RequestException
import org.eclipse.egit.github.core.service.OrganizationService
import org.eclipse.egit.github.core.service.TeamService
import org.eclipse.egit.github.core.service.UserService
import org.vaccineimpact.orderlyweb.db.Config
import org.vaccineimpact.orderlyweb.errors.BadConfigurationError

interface GithubAuthHelper
{
    fun initialise(token: String)

    //Checks that the Github user associated with the given token is permitted to authenticate with OrderlyWeb by
    //getting org/team membership for the user from Github API and comparing with permitted values in AppConfig.
    //Throws CredentialsException if check fails
    fun checkGithubUserCanAuthenticate()

    fun getUserEmail(): String

    fun getUser(): User
}

class GithubApiClientAuthHelper(private val appConfig: Config,
                                private val githubApiClient: GitHubClient = GitHubClient()) : GithubAuthHelper
{
    private var user: User? = null

    override fun initialise(token: String)
    {
        setClientToken(token)
        user = getGitHubUser()
    }

    override fun checkGithubUserCanAuthenticate()
    {
        checkInitialised()

        val githubOrg = appConfig["auth.github_org"]
        val teamName = appConfig["auth.github_team"]

        if (!githubOrg.isEmpty() && !userBelongToOrg(githubOrg, user!!))
        {
            throw CredentialsException("User is not a member of GitHub org $githubOrg or token does not include read:org scope")
        }
        if (!githubOrg.isEmpty() && !teamName.isEmpty() && !userBelongsToTeam(githubOrg, teamName, user!!))
        {
            throw CredentialsException("User is not a member of GitHub team $teamName")
        }
    }

    override fun getUserEmail(): String
    {
        checkInitialised()

        // If the GitHub user has no public email set, we need to make an extra call to get it
        val email = user!!.email ?: getEmailForUser()

        return email
    }

    override fun getUser(): User
    {
        checkInitialised()
        return user!!
    }

    private fun checkInitialised()
    {
        if (user == null)
            throw IllegalStateException("User has not been initialized")
    }

    private fun setClientToken(token: String)
    {
        if (CommonHelper.isBlank(token))
        {
            throw CredentialsException("Token cannot be blank")
        }

        githubApiClient.setOAuth2Token(token)
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


}