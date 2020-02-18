package org.vaccineimpact.orderlyweb.security.clients

import org.pac4j.core.context.WebContext
import org.pac4j.core.credentials.TokenCredentials
import org.pac4j.core.exception.CredentialsException
import org.pac4j.http.client.direct.HeaderClient
import org.vaccineimpact.orderlyweb.models.ErrorInfo
import org.vaccineimpact.orderlyweb.security.authentication.OrderlyWebBearerTokenAuthenticator
import org.vaccineimpact.orderlyweb.security.authentication.RSATokenVerifier
import org.vaccineimpact.orderlyweb.security.authorization.OrderlyAuthorizationGenerator

// This client receives the token as TokenCredentials and stores the result as JwtProfile
class JWTHeaderClient(helper: RSATokenVerifier) : OrderlyWebTokenCredentialClient, HeaderClient(
        "Authorization",
        "Bearer ",
        OrderlyWebBearerTokenAuthenticator(helper.signatureConfiguration, helper.expectedIssuer))
{
    override val errorInfo = ErrorInfo("bearer-token-invalid",
            "Bearer token not supplied in Authorization header, or bearer token was invalid")

    override fun clientInit()
    {
        setAuthorizationGenerator(OrderlyAuthorizationGenerator())
        super.clientInit()
    }

    override fun retrieveCredentials(context: WebContext): TokenCredentials?
    {
        return try
        {
            val credentials = credentialsExtractor.extract(context) ?: return null
            val t0 = System.currentTimeMillis()
            try
            {
                authenticator.validate(credentials, context)
            } finally
            {
                val t1 = System.currentTimeMillis()
                logger.debug("Credentials validation took: {} ms", t1 - t0)
            }
            credentials
        }
        catch (e: CredentialsException)
        {
            logger.info("Failed to retrieve or validate credentials: {}", e.message)
            logger.debug("Failed to retrieve or validate credentials", e)
            if (e.message != null)
            {
                val errorInfo = ErrorInfo("bearer-token-invalid", e.message!!)
                context.sessionStore.set(context, "credentials_exception", errorInfo)
            }
            null
        }
    }
}