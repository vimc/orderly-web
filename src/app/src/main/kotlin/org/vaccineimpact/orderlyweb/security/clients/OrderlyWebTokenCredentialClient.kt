package org.vaccineimpact.orderlyweb.security.clients

import org.pac4j.core.client.Client
import org.pac4j.core.credentials.TokenCredentials
import org.pac4j.core.profile.CommonProfile
import org.vaccineimpact.orderlyweb.models.ErrorInfo

interface OrderlyWebTokenCredentialClient: Client<TokenCredentials, CommonProfile>
{
    val errorInfo: ErrorInfo
}