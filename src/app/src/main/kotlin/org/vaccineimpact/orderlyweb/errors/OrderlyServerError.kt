package org.vaccineimpact.orderlyweb.errors

import org.vaccineimpact.orderlyweb.models.ErrorInfo

class OrderlyServerError(url: String, statusCode: Int) : OrderlyWebError(
    statusCode,
    listOf(ErrorInfo("orderly-server-error", "Orderly server request failed for url $url"))
)
