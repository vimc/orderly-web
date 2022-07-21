package org.vaccineimpact.orderlyweb.errors

import org.eclipse.jetty.http.HttpStatus
import org.vaccineimpact.orderlyweb.models.ErrorInfo

class InvalidOneTimeLinkToken(code: String, message: String) : OrderlyWebError(HttpStatus.BAD_REQUEST_400, listOf(
        ErrorInfo("invalid-token-$code", message)
))
