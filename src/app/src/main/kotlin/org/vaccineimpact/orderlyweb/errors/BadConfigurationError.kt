package org.vaccineimpact.orderlyweb.errors

class BadConfigurationError(message: String) : OrderlyWebError(500, listOf(
        org.vaccineimpact.orderlyweb.models.ErrorInfo("bad-config-error", message)
))