package org.vaccineimpact.orderlyweb.errors

import org.eclipse.jetty.http.HttpStatus
import org.vaccineimpact.orderlyweb.models.ErrorInfo

class InvalidOperationError(message: String): OrderlyWebError(HttpStatus.BAD_REQUEST_400, listOf(
        ErrorInfo("invalid-operation-error", message)
))
