package org.vaccineimpact.orderlyweb.security.providers

import com.github.salomonbrys.kotson.fromJson
import okhttp3.Headers
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.eclipse.egit.github.core.User
import org.pac4j.core.exception.CredentialsException
import org.pac4j.core.util.CommonHelper
import org.eclipse.egit.github.core.client.GitHubClient
import org.eclipse.egit.github.core.client.RequestException
import org.eclipse.egit.github.core.service.OrganizationService
import org.eclipse.egit.github.core.service.TeamService
import org.eclipse.egit.github.core.service.UserService
import org.vaccineimpact.orderlyweb.Serializer
import org.vaccineimpact.orderlyweb.db.Config
import org.vaccineimpact.orderlyweb.db.InvalidConfigurationKey
import org.vaccineimpact.orderlyweb.errors.BadConfigurationError
import org.vaccineimpact.orderlyweb.errors.UnexpectedError

interface GithubAuthHelper
{
    fun authenticate(token: String)

    fun checkGitHubOrgAndTeamMembership()

    fun getUserEmail(): String

    fun getUser(): User
}

class GithubApiClientAuthHelper(private val appConfig: Config,
                                private val githubApiClient: GitHubClient = GitHubClient(),
                                private val httpClient: OkHttpClient = OkHttpClient()) : GithubAuthHelper
{
    private var user: User? = null
    private var authToken: String? = null

    override fun authenticate(token: String)
    {
        authToken = token
        setClientToken(token)
        user = getGitHubUser()
    }

    override fun checkGitHubOrgAndTeamMembership()
    {
        checkAuthenticated()

        val githubOrg = appConfig["auth.github_org"]
        val teamName = appConfig["auth.github_team"]

        if (githubOrg.isEmpty())
        {
            throw InvalidConfigurationKey("auth.github_org", githubOrg)
        }
        val githubOrgId = getUserOrgId(githubOrg)
                ?: throw CredentialsException("User is not a member of GitHub org $githubOrg")
        if (!teamName.isEmpty() && !userBelongsToTeam(githubOrg, githubOrgId, teamName, user!!))
        {
            throw CredentialsException("User is not a member of GitHub team $teamName")
        }
    }

    override fun getUserEmail(): String
    {
        checkAuthenticated()

        // If the GitHub user has no public email set, we need to make an extra call to get it
        return user!!.email ?: getEmailForUser()
    }

    override fun getUser(): User
    {
        checkAuthenticated()
        return user!!
    }

    private fun checkAuthenticated()
    {
        if (user == null)
            throw IllegalStateException("User has not been authenticated")
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

    private fun getUserOrgId(githubOrg: String): Int?
    {
        //Check if user belongs to org and return org id if it does
        try
        {
            val userService = OrganizationService(githubApiClient)
            val orgs = userService.organizations
            return orgs.find{ it.login == githubOrg }?.id
        }
        catch (e: RequestException)
        {
            if (e.status == 403)
            {
                throw CredentialsException("GitHub token must include scope read:user")
            }
            else throw e
        }

    }

    private fun userBelongsToTeam(githubOrg: String, githubOrgId: Int, teamName: String, user: User): Boolean
    {
        val teamService = TeamService(githubApiClient)
        val team = teamService
                .getTeams(githubOrg).firstOrNull {
                    it.name == teamName
                }
                ?: throw BadConfigurationError("GitHub org $githubOrg has no team called $teamName")

        // See mrc-2966 - old team endpoints used by the client have been deprecated, so we manually use new endpoint
        // here for now
        val url = "https://api.github.com/organizations/${githubOrgId}/team/${team.id}/memberships/${user.login}"

        val headersBuilder = Headers.Builder()
        headersBuilder.add("Authorization", "token $authToken")
        headersBuilder.add("Accept", "application/vnd.github.beta+json")

        val request = Request.Builder()
                .url(url)
                .headers(headersBuilder.build())
                .build()

        val response = httpClient.newCall(request).execute()
        return when(response.code)
        {
            200 -> true
            404 -> false
            else -> throw CredentialsException("Unexpected status code from github api: ${response.code}")
        }
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
