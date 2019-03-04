package org.vaccineimpact.orderlyweb.errors

import org.vaccineimpact.orderlyweb.models.ErrorInfo

class RouteNotFound: MontaguError(404, listOf(ErrorInfo("not-found", "Requested resource not found.")))