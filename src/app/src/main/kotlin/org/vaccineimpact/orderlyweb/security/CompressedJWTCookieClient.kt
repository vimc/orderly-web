package org.vaccineimpact.orderlyweb.security

import org.pac4j.core.context.WebContext
import org.pac4j.core.credentials.TokenCredentials
import org.pac4j.http.client.direct.CookieClient
import org.pac4j.http.credentials.extractor.CookieExtractor
import org.vaccineimpact.api.models.ErrorInfo

class CompressedJWTCookieClientWrapper(helper: TokenVerifier) : MontaguCredentialClientWrapper
{
    override val client = CompressedJWTCookieClient(helper)
    override val errorInfo = ErrorInfo(
            "cookie-bearer-token-invalid",
            "Bearer token not supplied in cookie '${CompressedJWTCookieClient.cookie}', or bearer token was invalid"
    )
}

// This client receives the token as TokenCredentials and stores the result as JwtProfile
class CompressedJWTCookieClient(helper: TokenVerifier) : CookieClient(
        cookie,
        MontaguBearerTokenAuthenticator(helper.signatureConfiguration, helper.expectedIssuer)
)
{
    init
    {
        credentialsExtractor = CompressedCookieExtractor(cookie, name)
    }

    companion object
    {
        const val cookie = "montagu_jwt_token"
    }
}

class CompressedCookieExtractor(cookieName: String, name: String)
    : CookieExtractor(cookieName, name)
{
    override fun extract(context: WebContext?): TokenCredentials?
    {
        val wrapped = super.extract(context)
        return if (wrapped != null)
        {
            TokenCredentials(inflate(wrapped.token), wrapped.clientName)
        }
        else null
    }
}