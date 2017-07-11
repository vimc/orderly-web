package org.vaccineimpact.reporting_api.errors

import org.vaccineimpact.reporting_api.models.ErrorInfo

class UnableToConnectToDatabaseError(url: String) : MontaguError(500, listOf(
        ErrorInfo("database-connection-error", "Unable to establish connection to the database at $url")
))