package org.vaccineimpact.reporting_api.security

import org.pac4j.http.client.direct.ParameterClient
import org.vaccineimpact.api.models.ErrorInfo

// This client receives the token as TokenCredentials and stores the result as JwtProfile
class JWTParameterClientWrapper(helper: TokenVerifier,
                                tokenStore: OnetimeTokenStore,
                                url: String)
    : MontaguCredentialClientWrapper
{
    override val errorInfo = ErrorInfo("access-token-invalid", "Access token not supplied, or access token was invalid")
    override val client = JWTParameterClient(helper, tokenStore, url)
}

class JWTParameterClient(helper: TokenVerifier, tokenStore: OnetimeTokenStore, url: String) : ParameterClient(
        "access_token",
        MontaguOnetimeTokenAuthenticator(helper.signatureConfiguration,
                helper.expectedIssuer,
                tokenStore,
                url))
{
    init
    {
        this.isSupportGetRequest = true
    }
}