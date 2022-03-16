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

        println("Github User is ${user!!.login}")
        val orgs = user!!.organizations
        println("User orgs: " + orgs.toString())
    }

    override fun checkGitHubOrgAndTeamMembership()
    {
        println("checking membership")
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
        println("finished checking membership")
    }

    override fun getUserEmail(): String
    {
        checkAuthenticated()
        // If the GitHub user has no public email set, we need to make an extra call to get it
        return user!!.email ?: getEmailForUser()
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
        // TODO: work out what sort of errors are thrown that we need to deal with
        return github!!.getMyself()

        /*return try
        {
            github!!.getMyself()
        }
        catch (e: RequestException)
        {
            if (e.status == 401)
            {
                throw CredentialsException(e.message?:"")
            }
            else throw e
        }*/
    }

    private fun getOrg(orgName: String): GHOrganization
    {
        //TODO: deal with auth errors
        return github!!.getOrganization(orgName)
    }

    private fun userBelongsToOrg(org: GHOrganization): Boolean
    {
        //TODO: deal with auth errors

        return getUser().isMemberOf(org)

        /*try
        {
            val userService = OrganizationService(githubApiClient)
            val orgs = userService.organizations
            return orgs.map{ it.login }.contains(githubOrg)
        }
        catch (e: RequestException)
        {
            if (e.status == 403)
            {
                throw CredentialsException("GitHub token must include scope read:user")
            }
            else throw e
        }*/

    }

    private fun userBelongsToTeam(org: GHOrganization, teamName: String, user: GHUser): Boolean
    {
       //TODO: deal with auth errors
       val team = org.teams[teamName]
               ?: throw BadConfigurationError("GitHub org ${org.name} has no team called $teamName")
       return user.isMemberOf(team)

       /* val teamService = TeamService(githubApiClient)
        val team = teamService
                .getTeams(githubOrg).firstOrNull {
                    it.name == teamName
                }
                ?: throw BadConfigurationError("GitHub org $githubOrg has no team called $teamName")

        val members = teamService.getMembers(team.id)
        return members.map{ it.login }.contains(user.login)*/
    }

    private fun getEmailForUser(): String
    {
        //TODO: deal with auth errors
        try
        {
            return (getUser() as GHMyself).emails2.first().email
        }
        catch(e: GHFileNotFoundException) //todo: handle this or IOException
        {
            println("exception in getEmail")
            println(e.toString())
            throw(e)
        }

        /*return try
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
        }*/
    }

}
