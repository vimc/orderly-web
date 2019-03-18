package org.vaccineimpact.orderlyweb.security

import org.pac4j.core.context.Pac4jConstants
import org.pac4j.core.context.WebContext
import org.pac4j.core.credentials.TokenCredentials
import org.pac4j.core.credentials.authenticator.Authenticator
import org.pac4j.core.exception.CredentialsException
import org.pac4j.core.profile.CommonProfile
import org.pac4j.core.util.CommonHelper

class GithubAuthenticator : Authenticator<TokenCredentials>
{
    override fun validate(credentials: TokenCredentials?, context: WebContext)
    {
        if (credentials == null)
        {
            throwsException("No credentials supplied")
        }
        else
        {
            val token = credentials.token

            if (CommonHelper.isBlank(token))
            {
                throwsException("Token cannot be blank")
            }
            val username = validate(token)
            credentials.userProfile = CommonProfile().apply {
                this.addAttribute("url", "*")
                this.addAttribute(Pac4jConstants.USERNAME, username)
            }
        }
    }

    private fun validate(token: String): String
    {
        // TODO check github org
        // TODO save to db
        return "user.name"
    }

    private fun throwsException(message: String)
    {
        throw CredentialsException(message)
    }
}
