package org.vaccineimpact.orderlyweb.security.authentication

import com.github.scribejava.core.model.OAuth2AccessToken
import org.pac4j.core.client.IndirectClient
import org.pac4j.oauth.config.OAuth20Configuration
import org.pac4j.oauth.profile.creator.OAuth20ProfileCreator
import org.pac4j.core.context.WebContext
import org.pac4j.oauth.profile.github.GitHubProfile

class GithubOAuthProfileCreator(config: OAuth20Configuration, client: IndirectClient<*, *>) : OAuth20ProfileCreator<GitHubProfile>(config, client)
{
    override fun retrieveUserProfileFromToken(context: WebContext, accessToken: OAuth2AccessToken) : GitHubProfile
    {
        var result = super.retrieveUserProfileFromToken(context, accessToken)
        //Make credentials consistent with those produced by MontaguAuthenticator
        result.id = result.attributes["login"] as String //TODO: THis should actually be email
        return result
    }
}