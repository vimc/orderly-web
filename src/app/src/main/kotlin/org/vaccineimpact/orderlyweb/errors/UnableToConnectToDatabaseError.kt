package org.vaccineimpact.orderlyweb.errors

import org.eclipse.jetty.http.HttpStatus

class UnableToConnectToDatabaseError(url: String) : OrderlyWebError(
        HttpStatus.INTERNAL_SERVER_ERROR_500, listOf(
        org.vaccineimpact.orderlyweb.models.ErrorInfo("database-connection-error",
                "Unable to establish connection to the database at $url")
))
