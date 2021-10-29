package org.vaccineimpact.orderlyweb.errors

import org.vaccineimpact.orderlyweb.models.ErrorInfo

class BadRequest(message: String)
    : OrderlyWebError(400, listOf(ErrorInfo("bad-request", message)))


