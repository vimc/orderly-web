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

data class ErrorInfo(val code: String, val message: String)
{
    override fun toString(): String = message
}

