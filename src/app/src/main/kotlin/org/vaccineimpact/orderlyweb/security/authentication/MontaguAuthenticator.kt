package org.vaccineimpact.orderlyweb.security.authentication

import org.pac4j.core.context.WebContext
import org.pac4j.core.credentials.TokenCredentials
import org.pac4j.core.credentials.authenticator.Authenticator
import org.pac4j.core.exception.CredentialsException
import org.pac4j.core.profile.CommonProfile
import org.pac4j.core.util.CommonHelper
import org.pac4j.http.client.direct.CookieClient
import org.slf4j.LoggerFactory
import org.vaccineimpact.orderlyweb.models.ErrorInfo
import org.vaccineimpact.orderlyweb.security.authorization.AuthorizationRepository
import org.vaccineimpact.orderlyweb.security.clients.OrderlyWebTokenCredentialClient

class MontaguAuthenticator : Authenticator<TokenCredentials>
{
    private val logger = LoggerFactory.getLogger(MontaguAuthenticator::class.java)
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
            this.addAttribute("url", "*")
            this.setId(email)
        }
    }

    private fun validate(token: String): String
    {
        // TODO get real user details
        val result = khttp.get("http://localhost:8080/v1/user/modelling-groups/",
                headers = mapOf("Authorization" to "Bearer $token"))

        if (result.statusCode != 200)
        {
            throw loggedCredentialsException("Montagu cookie is not valid. Token may have expired.")
        }
        return "test.user@email.com"
    }

    private fun loggedCredentialsException(error: String): CredentialsException
    {
        logger.error(error)
        return CredentialsException(error)
    }
}