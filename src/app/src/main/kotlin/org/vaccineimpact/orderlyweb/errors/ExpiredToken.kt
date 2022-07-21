package org.vaccineimpact.orderlyweb.errors

import org.eclipse.jetty.http.HttpStatus
import org.vaccineimpact.orderlyweb.models.ErrorInfo

class ExpiredToken : OrderlyWebError(HttpStatus.UNAUTHORIZED_401, listOf(
        ErrorInfo("bearer-token-invalid", "Token has expired. Please request a new one.")
))
