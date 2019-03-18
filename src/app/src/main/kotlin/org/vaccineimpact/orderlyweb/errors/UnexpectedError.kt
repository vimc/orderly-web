package org.vaccineimpact.orderlyweb.errors

class UnexpectedError : OrderlyWebError(500, listOf(
        org.vaccineimpact.orderlyweb.models.ErrorInfo("unexpected-error", "An unexpected error occurred. Please see server logs for more details")
))