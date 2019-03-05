package org.vaccineimpact.orderlyweb.errors

import org.vaccineimpact.orderlyweb.models.ErrorInfo

class UnexpectedError : MontaguError(500, listOf(
        ErrorInfo("unexpected-error", "An unexpected error occurred. Please see server logs for more details")
))