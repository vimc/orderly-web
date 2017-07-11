package org.vaccineimpact.reporting_api.errors

import org.vaccineimpact.api.models.ErrorInfo
import org.vaccineimpact.api.models.Result
import org.vaccineimpact.api.models.ResultStatus

abstract class MontaguError(
        open val httpStatus: Int,
        val problems: Iterable<org.vaccineimpact.api.models.ErrorInfo>
) : Exception(formatProblemsIntoMessage(problems))
{
    open fun asResult() = org.vaccineimpact.api.models.Result(org.vaccineimpact.api.models.ResultStatus.FAILURE, null, problems)

    companion object
    {
        fun formatProblemsIntoMessage(problems: Iterable<org.vaccineimpact.api.models.ErrorInfo>): String
        {
            val joined = problems.map { it.message }.joinToString("\n")
            return "the following problems occurred:\n$joined"
        }
    }
}