package org.vaccineimpact.orderlyweb.errors

import org.eclipse.jetty.http.HttpStatus

class BadConfigurationError(message: String) : OrderlyWebError(
    HttpStatus.INTERNAL_SERVER_ERROR_500,
    listOf(org.vaccineimpact.orderlyweb.models.ErrorInfo("bad-config-error", message))
)
