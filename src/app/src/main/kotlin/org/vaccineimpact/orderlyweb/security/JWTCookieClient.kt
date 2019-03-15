package org.vaccineimpact.orderlyweb.security

import org.pac4j.http.client.direct.CookieClient
import org.vaccineimpact.orderlyweb.models.ErrorInfo

class JWTCookieClientWrapper(helper: TokenVerifier) : OrderlyWebCredentialClientWrapper
{
    override val client = JWTCookieClient(helper)
    override val errorInfo = ErrorInfo(
            "cookie-bearer-token-invalid",
            "Bearer token not supplied in cookie '${JWTCookieClient.cookie}', or bearer token was invalid"
    )
}

// This client receives the token as TokenCredentials and stores the result as JwtProfile
class JWTCookieClient(helper: TokenVerifier) : CookieClient(
        cookie,
        OrderlyWebBearerTokenAuthenticator(helper.signatureConfiguration, helper.expectedIssuer)
)
{
    companion object
    {
        const val cookie = "orderlyweb_jwt_token"
    }
}

