package org.vaccineimpact.orderlyweb.security.clients

import org.pac4j.http.client.direct.CookieClient
import org.vaccineimpact.orderlyweb.models.ErrorInfo
import org.vaccineimpact.orderlyweb.security.authentication.OrderlyWebBearerTokenAuthenticator
import org.vaccineimpact.orderlyweb.security.authentication.TokenVerifier
import org.vaccineimpact.orderlyweb.security.authorization.OrderlyAuthorizationGenerator

// This client receives the token as TokenCredentials and stores the result as JwtProfile
class JWTCookieClient(helper: TokenVerifier) : OrderlyWebTokenCredentialClient, CookieClient(
        cookie,
        OrderlyWebBearerTokenAuthenticator(helper.signatureConfiguration, helper.expectedIssuer)
)
{
    override fun internalInit(forceReinit: Boolean)
    {
        setAuthorizationGenerator(OrderlyAuthorizationGenerator())
        super.internalInit(forceReinit)
    }

    companion object
    {
        const val cookie = "orderlyweb_jwt_token"
    }

    override val errorInfo = ErrorInfo(
            "cookie-bearer-token-invalid",
            "Bearer token not supplied in cookie '$cookie', or bearer token was invalid"
    )
}
