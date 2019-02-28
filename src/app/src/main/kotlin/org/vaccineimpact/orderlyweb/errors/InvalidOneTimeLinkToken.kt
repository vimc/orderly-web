package org.vaccineimpact.orderlyweb.errors

import org.vaccineimpact.orderlyweb.models.ErrorInfo

class InvalidOneTimeLinkToken(code: String, message: String) : MontaguError(400, listOf(
        ErrorInfo("invalid-token-$code", message)
))