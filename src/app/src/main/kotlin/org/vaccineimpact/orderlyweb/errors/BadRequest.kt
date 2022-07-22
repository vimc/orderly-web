package org.vaccineimpact.orderlyweb.errors

import org.eclipse.jetty.http.HttpStatus
import org.vaccineimpact.orderlyweb.models.ErrorInfo

class BadRequest(messages: List<String>):
        OrderlyWebError(HttpStatus.BAD_REQUEST_400, messages.map{ ErrorInfo("bad-request", it) })
{
    constructor(message: String) : this(listOf(message))
}
