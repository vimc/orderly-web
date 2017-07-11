package org.vaccineimpact.reporting_api.errors

import org.vaccineimpact.reporting_api.models.ErrorInfo

class BadDatabaseConstant(val value: String, val type: String) : MontaguError(500, listOf(
        ErrorInfo("database-error", "An unexpected value of '$value' was found for '$type'")
))