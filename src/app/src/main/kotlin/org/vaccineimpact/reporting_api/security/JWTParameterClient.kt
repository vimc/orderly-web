package org.vaccineimpact.reporting_api.security

import org.pac4j.http.client.direct.ParameterClient
import org.vaccineimpact.api.models.ErrorInfo

class JWTParameterClientWrapper(helper: TokenVerifier,
                                tokenStore: OnetimeTokenStore)
    : MontaguCredentialClientWrapper
{
    override val errorInfo = ErrorInfo("access-token-invalid", "Access token not supplied, or access token was invalid")
    override val client = JWTParameterClient(helper, tokenStore)
}

// This client receives the token as TokenCredentials and stores the result as JwtProfile
class JWTParameterClient(helper: TokenVerifier, tokenStore: OnetimeTokenStore) : ParameterClient(
        "access_token",
        MontaguOnetimeTokenAuthenticator(helper.signatureConfiguration,
                helper.expectedIssuer,
                tokenStore))
{
    init
    {
        this.isSupportGetRequest = true
    }
}