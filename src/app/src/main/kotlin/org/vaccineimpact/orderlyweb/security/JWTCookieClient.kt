package org.vaccineimpact.orderlyweb.security

import org.pac4j.core.context.WebContext
import org.pac4j.core.credentials.TokenCredentials
import org.pac4j.http.client.direct.CookieClient
import org.pac4j.http.credentials.extractor.CookieExtractor
import org.vaccineimpact.orderlyweb.models.ErrorInfo

class JWTCookieClientWrapper(helper: TokenVerifier) : MontaguCredentialClientWrapper
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
        MontaguBearerTokenAuthenticator(helper.signatureConfiguration, helper.expectedIssuer)
)
{
    init
    {
        credentialsExtractor = CookieExtractor(cookie, name)
    }

    companion object
    {
        const val cookie = "orderlyweb_jwt_token"
    }
}

