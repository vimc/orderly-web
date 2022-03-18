package org.vaccineimpact.orderlyweb.security.providers

import org.kohsuke.github.*
import org.pac4j.core.exception.CredentialsException
import org.pac4j.core.util.CommonHelper
import org.vaccineimpact.orderlyweb.db.Config
import org.vaccineimpact.orderlyweb.db.InvalidConfigurationKey
import org.vaccineimpact.orderlyweb.errors.BadConfigurationError

interface GithubAuthHelper
{
    fun authenticate(token: String)

    fun checkGitHubOrgAndTeamMembership()

    fun getUserEmail(): String

    fun getUser(): GHUser
}

class GithubApiClientAuthHelper(private val appConfig: Config,
                                private val githubBuilder: GitHubBuilder = GitHubBuilder()) : GithubAuthHelper
{
    private var github: GitHub? = null
    private var user: GHUser? = null

    override fun authenticate(token: String)
    {
        connectToClient(token)
        user = getGitHubUser()
    }

    override fun checkGitHubOrgAndTeamMembership()
    {
        checkAuthenticated()

        val orgName = appConfig["auth.github_org"]
        val teamName = appConfig["auth.github_team"]

        if (orgName.isEmpty())
        {
            throw InvalidConfigurationKey("auth.github_org", orgName)
        }

        val org = getOrg(orgName)
        if (!userBelongsToOrg(org))
        {
            throw CredentialsException("User is not a member of GitHub org $orgName")
        }
        if (!teamName.isEmpty() && !userBelongsToTeam(org, teamName, user!!))
        {
            throw CredentialsException("User is not a member of GitHub team $teamName")
        }
    }

    override fun getUserEmail(): String
    {
        checkAuthenticated()
        // If the GitHub user has no public email set, we need to make an extra call to get it
        val result = user!!.email ?: getEmailForUser()
        return result
    }

    override fun getUser(): GHUser
    {
        checkAuthenticated()
        return user!!
    }

    private fun checkAuthenticated()
    {
        if (user == null)
            throw IllegalStateException("User has not been authenticated")
    }

    private fun connectToClient(token: String)
    {
        if (CommonHelper.isBlank(token))
        {
            throw CredentialsException("Token cannot be blank")
        }

        github = githubBuilder.withOAuthToken(token).build()
    }

    private fun getGitHubUser(): GHUser
    {
        try
        {
            return github!!.myself
        }
        catch(e: HttpException)
        {
           if (e.responseCode == 401)
           {
               throw CredentialsException(e.message?:"")
           }
            else throw e
        }
    }

    private fun getOrg(orgName: String): GHOrganization
    {
        try
        {
            return github!!.getOrganization(orgName)
        }
        catch(e: HttpException)
        {
            if (e.responseCode == 401)
            {
                throw CredentialsException(e.message?:"")
            }
            else throw e
        }
    }

    private fun userBelongsToOrg(org: GHOrganization): Boolean
    {
        return getUser().isMemberOf(org)
    }

    private fun userBelongsToTeam(org: GHOrganization, teamName: String, user: GHUser): Boolean
    {
       val team = org.teams[teamName]
               ?: throw BadConfigurationError("GitHub org ${org.name} has no team called $teamName")
       return user.isMemberOf(team)
    }

    private fun getEmailForUser(): String
    {
        try
        {
            return (getUser() as GHMyself).emails2.first().email
        }
        catch(e: GHFileNotFoundException)
        {
            throw CredentialsException("GitHub token must include scope user:email")
        }
    }
}
