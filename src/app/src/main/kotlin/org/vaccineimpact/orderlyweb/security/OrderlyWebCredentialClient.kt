package org.vaccineimpact.orderlyweb.security

import org.vaccineimpact.orderlyweb.models.ErrorInfo

interface OrderlyWebCredentialClient
{
    val errorInfo: ErrorInfo
}
