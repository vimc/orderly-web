package org.vaccineimpact.orderlyweb.security

import org.pac4j.core.context.WebContext
import org.pac4j.core.credentials.TokenCredentials
import org.pac4j.core.credentials.extractor.HeaderExtractor
import org.pac4j.http.client.direct.HeaderClient
import org.vaccineimpact.orderlyweb.models.ErrorInfo

class CompressedJWTHeaderClientWrapper(helper: TokenVerifier) : CredentialClientWrapper
{
    override val errorInfo = ErrorInfo("bearer-token-invalid", "Bearer token not supplied in Authorization header, or bearer token was invalid")
    override val client = CompressedJWTHeaderClient(helper)
}

// This client receives the token as TokenCredentials and stores the result as JwtProfile
class CompressedJWTHeaderClient(helper: TokenVerifier) : HeaderClient(
        "Authorization",
        "Bearer ",
        MontaguBearerTokenAuthenticator(helper.signatureConfiguration, helper.expectedIssuer))
{
    init
    {
        credentialsExtractor = CompressedHeaderExtractor(headerName, prefixHeader, name)
        setAuthorizationGenerator { _, profile -> extractPermissionsFromToken(profile) }
    }
}

class CompressedHeaderExtractor(headerName: String, prefixHeader: String, name: String)
    : HeaderExtractor(headerName, prefixHeader, name)
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