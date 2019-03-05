package org.vaccineimpact.orderlyweb.errors

class UnableToConnectToDatabaseError(url: String) : MontaguError(500, listOf(
        org.vaccineimpact.api.models.ErrorInfo("database-connection-error", "Unable to establish connection to the database at $url")
))