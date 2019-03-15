package org.vaccineimpact.orderlyweb.security

import org.pac4j.core.client.DirectClient
import org.pac4j.core.credentials.TokenCredentials
import org.pac4j.core.profile.CommonProfile
import org.vaccineimpact.orderlyweb.models.ErrorInfo

interface MontaguCredentialClientWrapper
{
    val errorInfo: ErrorInfo
    val client: DirectClient<TokenCredentials, CommonProfile>
}
