package org.vaccineimpact.reporting_api.errors

class UnexpectedError : MontaguError(500, listOf(
        org.vaccineimpact.api.models.ErrorInfo("unexpected-error", "An unexpected error occurred. Please see server logs for more details")
))