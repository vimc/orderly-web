package org.vaccineimpact.orderlyweb.security

import org.pac4j.core.context.WebContext
import org.pac4j.http.client.direct.CookieClient
import org.vaccineimpact.orderlyweb.models.ErrorInfo

// This client receives the token as TokenCredentials and stores the result as JwtProfile
class JWTCookieClient(helper: TokenVerifier): OrderlyWebTokenCredentialClient, CookieClient(
        cookie,
        OrderlyWebBearerTokenAuthenticator(helper.signatureConfiguration, helper.expectedIssuer)
)
{
    override fun clientInit(context: WebContext?)
    {
        addAuthorizationGenerator{ _, profile -> extractPermissionsFromToken(profile) }
        super.clientInit(context)
    }

    companion object
    {
        const val cookie = "orderlyweb_jwt_token"
    }

    override val errorInfo = ErrorInfo(
            "cookie-bearer-token-invalid",
            "Bearer token not supplied in cookie '${JWTCookieClient.cookie}', or bearer token was invalid"
    )
}
