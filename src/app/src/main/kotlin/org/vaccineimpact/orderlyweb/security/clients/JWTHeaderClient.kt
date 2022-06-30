package org.vaccineimpact.orderlyweb.security.clients

import org.pac4j.core.context.WebContext
import org.pac4j.core.context.session.SessionStore
import org.pac4j.core.credentials.Credentials
import org.pac4j.core.exception.CredentialsException
import org.pac4j.http.client.direct.HeaderClient
import org.vaccineimpact.orderlyweb.models.ErrorInfo
import org.vaccineimpact.orderlyweb.security.authentication.OrderlyWebBearerTokenAuthenticator
import org.vaccineimpact.orderlyweb.security.authentication.RSATokenVerifier
import org.vaccineimpact.orderlyweb.security.authorization.OrderlyAuthorizationGenerator
import java.util.*

// This client receives the token as TokenCredentials and stores the result as JwtProfile
class JWTHeaderClient(helper: RSATokenVerifier) : OrderlyWebTokenCredentialClient, HeaderClient(
        "Authorization",
        "Bearer ",
        OrderlyWebBearerTokenAuthenticator(helper.signatureConfiguration, helper.expectedIssuer)
)
{
    override val errorInfo = ErrorInfo(
            "bearer-token-invalid",
            "Bearer token not supplied in Authorization header, or bearer token was invalid"
    )

    override fun internalInit(forceReinit: Boolean)
    {
        setAuthorizationGenerator(OrderlyAuthorizationGenerator())
        super.internalInit(forceReinit)
    }

    override fun retrieveCredentials(context: WebContext, sessionStore: SessionStore): Optional<Credentials>
    {
        return try
        {
            val optCredentials = credentialsExtractor.extract(context, sessionStore)
            optCredentials.ifPresent { credentials: Credentials? ->
                val t0 = System.currentTimeMillis()
                try
                {
                    authenticator.validate(credentials, context, sessionStore)
                } finally
                {
                    val t1 = System.currentTimeMillis()
                    logger.debug("Credentials validation took: {} ms", t1 - t0)
                }
            }
            optCredentials
        }
        catch (e: CredentialsException)
        {
            sessionStore.set(context, "token_exception", e)
            logger.info("Failed to retrieve or validate credentials: {}", e.message)
            logger.debug("Failed to retrieve or validate credentials", e)
            Optional.empty()
        }
    }
}
