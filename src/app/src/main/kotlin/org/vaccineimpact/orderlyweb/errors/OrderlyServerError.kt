package org.vaccineimpact.orderlyweb.errors

import org.vaccineimpact.orderlyweb.models.ErrorInfo

class OrderlyServerError(url: String, statusCode: Int) : OrderlyWebError(statusCode, listOf(
        ErrorInfo("orderly-server-error", "Orderly server request failed for url $url")
))

class OrderlyServerResponseError(url: String) : OrderlyWebError(500, listOf(
        ErrorInfo("orderly-server-error", "Unexpected response type for url $url")
))
