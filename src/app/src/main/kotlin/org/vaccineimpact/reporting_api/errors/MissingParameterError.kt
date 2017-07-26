package org.vaccineimpact.reporting_api.errors

import org.vaccineimpact.api.models.ErrorInfo

class MissingParameterError(parameterName: String)
    : MontaguError(400, listOf(ErrorInfo("bad-request", "Missing parameter '$parameterName'")))
