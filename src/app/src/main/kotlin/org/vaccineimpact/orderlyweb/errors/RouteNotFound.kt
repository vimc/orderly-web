package org.vaccineimpact.orderlyweb.errors

import org.eclipse.jetty.http.HttpStatus
import org.vaccineimpact.orderlyweb.models.ErrorInfo

class RouteNotFound : OrderlyWebError(
    HttpStatus.NOT_FOUND_404,
    listOf(ErrorInfo("not-found", "Requested resource not found."))
)
