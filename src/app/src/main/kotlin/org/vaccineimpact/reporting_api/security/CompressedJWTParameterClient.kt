package org.vaccineimpact.reporting_api.security

import org.pac4j.core.context.WebContext
import org.pac4j.core.credentials.TokenCredentials
import org.pac4j.core.credentials.extractor.ParameterExtractor
import org.pac4j.http.client.direct.ParameterClient
import org.vaccineimpact.api.models.ErrorInfo

class CompressedJWTParameterClientWrapper(helper: TokenVerifier,
                                          tokenStore: OnetimeTokenStore)
    : MontaguCredentialClientWrapper
{
    override val errorInfo = ErrorInfo("onetime-token-invalid", "Onetime token not supplied, or onetime token was invalid")
    override val client = CompressedJWTParameterClient(helper, tokenStore)
}

// This client receives the token as TokenCredentials and stores the result as JwtProfile
class CompressedJWTParameterClient(helper: TokenVerifier, tokenStore: OnetimeTokenStore) : ParameterClient(
        "access_token",
        MontaguOnetimeTokenAuthenticator(helper.signatureConfiguration,
                helper.expectedIssuer,
                tokenStore))
{
    init
    {
        this.isSupportGetRequest = true
        credentialsExtractor = CompressedParameterExtractor(
                parameterName, isSupportGetRequest, isSupportPostRequest, name)
    }
}

class CompressedParameterExtractor(
        parameterName: String,
        supportGetRequest: Boolean,
        supportPostRequest: Boolean,
        clientName: String)
    : ParameterExtractor(parameterName, supportGetRequest, supportPostRequest, clientName)
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