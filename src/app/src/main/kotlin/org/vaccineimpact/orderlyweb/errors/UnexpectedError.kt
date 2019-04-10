package org.vaccineimpact.orderlyweb.errors

import org.apache.commons.lang3.RandomStringUtils
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.models.ErrorInfo

val supportAddress = AppConfig()["app.email"]

// The purpose of the code, even though it's not used anywhere else, is that the full text of the error gets logged.
// So if a user gives you a code like UAA-BBB-CCC you can then search through the logs to find and see the associated
// stack trace.
class UnexpectedError : OrderlyWebError(500, listOf(ErrorInfo(
        "unexpected-error",
        "An unexpected error occurred. Please contact support at $supportAddress and quote this code: ${newCode()}"
)))

private fun newCode(): String
{
    fun r(count: Int) = RandomStringUtils.randomAlphabetic(count).toLowerCase()
    return "u${r(2)}-${r(3)}-${r(3)}"
}
