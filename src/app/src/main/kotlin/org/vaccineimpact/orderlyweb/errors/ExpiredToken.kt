package org.vaccineimpact.orderlyweb.errors

import org.vaccineimpact.orderlyweb.models.ErrorInfo

class ExpiredToken : OrderlyWebError(401, listOf(
        ErrorInfo("bearer-token-invalid", "Token has expired. Please request a new one.")
))