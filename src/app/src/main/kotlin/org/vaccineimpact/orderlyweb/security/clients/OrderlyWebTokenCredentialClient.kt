package org.vaccineimpact.orderlyweb.security.clients

import org.pac4j.core.client.Client
import org.vaccineimpact.orderlyweb.models.ErrorInfo

interface OrderlyWebTokenCredentialClient : Client
{
    val errorInfo: ErrorInfo
}
