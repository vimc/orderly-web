package org.vaccineimpact.reporting_api.security

import org.pac4j.http.client.direct.ParameterClient
import org.vaccineimpact.api.models.ErrorInfo

// This client receives the token as TokenCredentials and stores the result as JwtProfile
class JWTParameterClientWrapper(helper: TokenVerifier)
    : MontaguDirectClient
{
    override val errorInfo = ErrorInfo("access-token-invalid", "Access token not supplied, or access token was invalid")

    override val client = JWTParameterClient(helper)

}

class JWTParameterClient(helper: TokenVerifier) : ParameterClient(
        "access_token",
        MontaguTokenAuthenticator(helper.signatureConfiguration, helper.expectedIssuer))
{
    init
    {
        this.isSupportGetRequest = true
    }
}