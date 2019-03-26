package org.vaccineimpact.orderlyweb.security.authentication

import org.pac4j.core.context.WebContext
import org.pac4j.core.credentials.TokenCredentials
import org.pac4j.core.credentials.authenticator.Authenticator
import org.pac4j.core.exception.CredentialsException
import org.pac4j.core.profile.CommonProfile
import org.pac4j.core.util.CommonHelper
import org.slf4j.LoggerFactory
import org.vaccineimpact.orderlyweb.db.UserRepository
import org.vaccineimpact.orderlyweb.models.UserSource

class MontaguAuthenticator(private val userRepository: UserRepository,
                           private val montaguClient: MontaguAPIClient
) : Authenticator<TokenCredentials>
{
    private val logger = LoggerFactory.getLogger(MontaguAuthenticator::class.java)

    override fun validate(credentials: TokenCredentials?, context: WebContext?)
    {
        if (credentials == null)
        {
            throw loggedCredentialsException("No credentials supplied")
        }

        val token = credentials.token

        if (CommonHelper.isBlank(token))
        {
            throw loggedCredentialsException("Token cannot be blank")
        }

        val email = validate(token)

        credentials.userProfile = CommonProfile().apply {
            this.addAttribute("url", "*")
            this.setId(email)
        }
    }

    private fun validate(token: String): String
    {
        val user = montaguClient.getUserDetails(token)

        try
        {
            userRepository.addUser(user.email, user.username, user.displayName ?: "", UserSource.Montagu)
        }
        catch (e: MontaguAPIException)
        {
            throw loggedCredentialsException("Montagu authentication failed with status ${e.status} and message ${e.message}")
        }

        return user.email
    }

    private fun loggedCredentialsException(error: String): CredentialsException
    {
        logger.error(error)
        return CredentialsException(error)
    }
}