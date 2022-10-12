package org.vaccineimpact.orderlyweb.errors

import org.eclipse.jetty.http.HttpStatus
import org.vaccineimpact.orderlyweb.models.ErrorInfo

class ViewModelError(message: String) : OrderlyWebError(
        HttpStatus.INTERNAL_SERVER_ERROR_500,
        listOf(ErrorInfo("view-model-error", message))
)
