package org.vaccineimpact.reporting_api.security
import org.pac4j.core.authorization.authorizer.Authorizer
import org.pac4j.core.client.DirectClient
import org.pac4j.core.credentials.TokenCredentials
import org.pac4j.core.profile.CommonProfile
import org.vaccineimpact.api.models.ErrorInfo

interface MontaguCredentialClientWrapper
{
    val errorInfo: ErrorInfo
    val client : DirectClient<TokenCredentials, CommonProfile>
  //  val authorisers : List<Authorizer<CommonProfile>>
}
