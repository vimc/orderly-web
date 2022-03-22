package org.vaccineimpact.orderlyweb.errors

import org.vaccineimpact.orderlyweb.models.ErrorInfo

class BadRequest(messages: List<String>)
    : OrderlyWebError(400, messages.map { ErrorInfo("bad-request", it) })
{
    constructor(message: String) : this(listOf(message))
}
