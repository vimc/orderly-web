package org.vaccineimpact.orderlyweb.security

import org.pac4j.core.credentials.extractor.HeaderExtractor
import org.pac4j.http.client.direct.HeaderClient
import org.vaccineimpact.api.models.ErrorInfo

class JWTHeaderClientWrapper(helper: TokenVerifier) : MontaguCredentialClientWrapper
{
    override val errorInfo = ErrorInfo("bearer-token-invalid", "Bearer token not supplied in Authorization header, or bearer token was invalid")
    override val client = JWTHeaderClient(helper)
}

// This client receives the token as TokenCredentials and stores the result as JwtProfile
class JWTHeaderClient(helper: TokenVerifier) : HeaderClient(
        "Authorization",
        "Bearer ",
        MontaguBearerTokenAuthenticator(helper.signatureConfiguration, helper.expectedIssuer))
{
    init
    {
        credentialsExtractor = HeaderExtractor(headerName, prefixHeader, name)
    }
}