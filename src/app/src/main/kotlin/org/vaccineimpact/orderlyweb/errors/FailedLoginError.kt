package org.vaccineimpact.orderlyweb.errors

import org.vaccineimpact.orderlyweb.models.ErrorInfo

class FailedLoginError(message: String) : OrderlyWebError(401,
        listOf(ErrorInfo("login-failed", message)))