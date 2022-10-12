package org.vaccineimpact.orderlyweb.errors

import com.google.gson.JsonSyntaxException
import org.eclipse.jetty.http.HttpStatus

class UnableToParseJsonError(e: JsonSyntaxException) : OrderlyWebError(
    HttpStatus.BAD_REQUEST_400,
    listOf(
        org.vaccineimpact.orderlyweb.models.ErrorInfo("bad-json", formatMessage(e))
    )
)
{
    companion object
    {
        fun formatMessage(e: JsonSyntaxException): String
        {
            // We munge the exception to make it more readable - this is okay, because if the exception is not in
            // the format we expect, the replace call will just have no effect
            val inner = e.message?.replace("com.google.gson.stream.MalformedJsonException: ", "")
            return "Unable to parse supplied JSON: $inner"
        }
    }
}
