package org.vaccineimpact.reporting_api.security

import org.pac4j.http.client.direct.HeaderClient
import org.vaccineimpact.api.models.ErrorInfo

// This client receives the token as TokenCredentials and stores the result as JwtProfile
class JWTHeaderClientWrapper(helper: TokenVerifier) : MontaguDirectClient
{
    override val errorInfo = ErrorInfo("bearer-token-invalid", "Bearer token not supplied in Authorization header, or bearer token was invalid")

    override val client = JWTHeaderClient(helper)
}

class JWTHeaderClient(helper: TokenVerifier) : HeaderClient(
        "Authorization",
        "Bearer ",
        MontaguTokenAuthenticator(helper.signatureConfiguration, helper.expectedIssuer))