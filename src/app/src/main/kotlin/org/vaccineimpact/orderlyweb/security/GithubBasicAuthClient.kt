package org.vaccineimpact.orderlyweb.security

import org.pac4j.core.client.DirectClient
import org.pac4j.core.context.HttpConstants
import org.pac4j.core.context.WebContext
import org.pac4j.core.credentials.UsernamePasswordCredentials
import org.pac4j.core.credentials.extractor.BasicAuthExtractor
import org.pac4j.core.profile.CommonProfile

class GithubBasicAuthClient: DirectClient<UsernamePasswordCredentials, CommonProfile>()
{
    override fun clientInit(context: WebContext?)
    {
        defaultCredentialsExtractor(BasicAuthExtractor(
                HttpConstants.AUTHORIZATION_HEADER,
                "GithubBasic ", this.name))

        defaultAuthenticator(GithubAuthenticator())
    }
}