package org.vaccineimpact.orderlyweb.security.authentication

import org.pac4j.core.context.WebContext
import org.pac4j.core.credentials.TokenCredentials
import org.pac4j.core.credentials.authenticator.Authenticator
import org.pac4j.core.exception.CredentialsException
import org.pac4j.core.profile.CommonProfile
import org.pac4j.core.util.CommonHelper
import org.vaccineimpact.orderlyweb.db.UserRepository
import org.vaccineimpact.orderlyweb.models.UserSource
import org.vaccineimpact.orderlyweb.security.providers.MontaguAPIClient
import org.vaccineimpact.orderlyweb.security.providers.MontaguAPIException

class MontaguAuthenticator(private val userRepository: UserRepository,
                           private val montaguClient: MontaguAPIClient
) : Authenticator<TokenCredentials>
{
    override fun validate(credentials: TokenCredentials?, context: WebContext?)
    {
        if (credentials == null)
        {
            throw CredentialsException("No credentials supplied")
        }

        val token = credentials.token

        if (CommonHelper.isBlank(token))
        {
            throw CredentialsException("Token cannot be blank")
        }

        val email = validate(token)

        credentials.userProfile = CommonProfile().apply {
            this.id = email
        }
    }

    private fun validate(token: String): String
    {
        val user = try
        {
            montaguClient.getUserDetails(token)
        }
        catch (e: MontaguAPIException)
        {
            throw CredentialsException("Montagu authentication failed with status ${e.status} and message ${e.message}")
        }
        userRepository.addUser(user.email, user.username, user.name ?: "", UserSource.Montagu)
        return user.email
    }

}