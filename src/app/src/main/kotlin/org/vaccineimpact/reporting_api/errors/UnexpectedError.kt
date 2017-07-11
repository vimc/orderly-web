package org.vaccineimpact.reporting_api.errors

import org.vaccineimpact.reporting_api.models.ErrorInfo

class UnexpectedError : MontaguError(500, listOf(
        ErrorInfo("unexpected-error", "An unexpected error occurred. Please see server logs for more details")
))