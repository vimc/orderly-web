package org.vaccineimpact.orderlyweb.errors

abstract class OrderlyWebError(
        open val httpStatus: Int,
        val problems: Iterable<org.vaccineimpact.orderlyweb.models.ErrorInfo>
) : Exception(formatProblemsIntoMessage(problems))
{
    open fun asResult() = org.vaccineimpact.orderlyweb.models.Result(
            org.vaccineimpact.orderlyweb.models.ResultStatus.FAILURE, null, problems)

    companion object
    {
        fun formatProblemsIntoMessage(problems: Iterable<org.vaccineimpact.orderlyweb.models.ErrorInfo>): String
        {
            val joined = problems.map { it.message }.joinToString("\n")
            return "the following problems occurred:\n$joined"
        }
    }
}
