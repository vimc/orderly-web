package org.vaccineimpact.orderlyweb.errors

import org.vaccineimpact.orderlyweb.models.ErrorInfo

class MissingParameterError(parameterName: String)
    : MontaguError(400, listOf(ErrorInfo("bad-request", "Missing parameter '$parameterName'")))
