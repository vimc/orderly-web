package org.vaccineimpact.orderlyweb.errors

import org.eclipse.jetty.http.HttpStatus
import org.vaccineimpact.orderlyweb.models.ErrorInfo

class MissingParameterError(parameterName: String)
    : OrderlyWebError(HttpStatus.BAD_REQUEST_400,
        listOf(ErrorInfo("bad-request", "Missing parameter '$parameterName'")))
