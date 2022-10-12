package org.vaccineimpact.orderlyweb.errors

import org.eclipse.jetty.http.HttpStatus
import org.vaccineimpact.orderlyweb.models.ErrorInfo

class DataError(message: String) : OrderlyWebError(
        HttpStatus.INTERNAL_SERVER_ERROR_500,
        listOf(ErrorInfo("data-error", message))
)
