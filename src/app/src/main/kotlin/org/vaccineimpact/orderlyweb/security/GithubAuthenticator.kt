package org.vaccineimpact.orderlyweb.security

import org.pac4j.core.context.Pac4jConstants
import org.pac4j.core.context.WebContext
import org.pac4j.core.credentials.UsernamePasswordCredentials
import org.pac4j.core.credentials.authenticator.Authenticator
import org.pac4j.core.exception.CredentialsException
import org.pac4j.core.profile.CommonProfile
import org.pac4j.core.util.CommonHelper
import org.vaccineimpact.orderlyweb.models.User
import java.time.Instant

class GithubAuthenticator : Authenticator<UsernamePasswordCredentials>
{
    override fun validate(credentials: UsernamePasswordCredentials?, context: WebContext?)
    {
        if (credentials == null)
        {
            throwsException("No credentials supplied")
        }
        else
        {
            val username = credentials.username
            val token = credentials.password
            if (CommonHelper.isBlank(username))
            {
                throwsException("Username cannot be blank")
            }
            if (CommonHelper.isBlank(token))
            {
                throwsException("Token cannot be blank")
            }
            validate(username, token)
            credentials.userProfile = CommonProfile().apply {
                this.addAttribute(Pac4jConstants.USERNAME, username)
            }
        }
    }

    private fun validate(username: String, token: String): User
    {
        // TODO check github org
        // TODO save to db

        return User(username, username, username, Instant.now())
    }

    private fun throwsException(message: String)
    {
        throw CredentialsException(message)
    }
}
