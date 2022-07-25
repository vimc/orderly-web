package org.vaccineimpact.orderlyweb.errors

import org.apache.commons.lang3.RandomStringUtils
import org.eclipse.jetty.http.HttpStatus
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.models.ErrorInfo

val supportAddress = AppConfig()["app.email"]

// The purpose of the code, even though it's not used anywhere else, is that the full text of the error gets logged.
// So if a user gives you a code like UAA-BBB-CCC you can then search through the logs to find and see the associated
// stack trace.
class UnexpectedError : OrderlyWebError(HttpStatus.INTERNAL_SERVER_ERROR_500, listOf(ErrorInfo(
        "unexpected-error",
        "An unexpected error occurred. Please contact support at $supportAddress and quote this code: ${newCode()}"
)))

const val RANDOM_CHAR_FIRST_LENGTH = 2
const val RANDOM_CHAR_LENGTH = 3

private fun newCode(): String
{
    fun r(count: Int) = RandomStringUtils.randomAlphabetic(count).lowercase()
    return "u${r(RANDOM_CHAR_FIRST_LENGTH)}-${r(RANDOM_CHAR_LENGTH)}-${r(RANDOM_CHAR_LENGTH)}"
}
