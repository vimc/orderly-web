package org.vaccineimpact.orderlyweb.models

class Result(val status: ResultStatus, data: Any?, errors: Iterable<ErrorInfo>)
{
    val data = data ?: ""
    val errors = errors.toList()
}

enum class ResultStatus
{
    SUCCESS, FAILURE
}

data class ErrorInfo(val error: String, val detail: String)
{
    override fun toString(): String = detail
}
