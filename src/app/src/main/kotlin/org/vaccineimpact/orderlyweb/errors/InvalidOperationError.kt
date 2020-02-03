package org.vaccineimpact.orderlyweb.errors

import org.vaccineimpact.orderlyweb.models.ErrorInfo

class InvalidOperationError(message: String): OrderlyWebError(400, listOf(
        ErrorInfo("invalid-operation-error", message)
))