package org.vaccineimpact.orderlyweb.errors

import org.vaccineimpact.orderlyweb.models.ErrorInfo

class MissingParameterError(parameterName: String)
    : OrderlyWebError(400, listOf(ErrorInfo("bad-request", "Missing parameter '$parameterName'")))
