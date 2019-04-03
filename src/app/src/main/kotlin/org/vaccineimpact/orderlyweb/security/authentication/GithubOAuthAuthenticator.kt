package org.vaccineimpact.orderlyweb.security.authentication

import org.pac4j.oauth.config.OAuth20Configuration
import org.pac4j.oauth.credentials.authenticator.OAuth20Authenticator
import org.pac4j.core.client.IndirectClient
import org.pac4j.core.context.WebContext
import org.pac4j.core.profile.CommonProfile
import org.pac4j.oauth.profile.github.GitHubProfile
import org.pac4j.oauth.credentials.OAuth20Credentials


class GithubOAuthAuthenticator(config: OAuth20Configuration, client: IndirectClient<*,*>) : OAuth20Authenticator(config, client)
{
    override fun validate(credentials: OAuth20Credentials, context: WebContext?)
    {
        super.validate(credentials, context)

    }

    //TODO: Ensure that user is a member of the configured Github org and team who are allowed access
}