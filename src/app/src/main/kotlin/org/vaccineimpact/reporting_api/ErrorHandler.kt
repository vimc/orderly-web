package org.vaccineimpact.reporting_api

import com.google.gson.JsonSyntaxException
import org.slf4j.LoggerFactory
import org.vaccineimpact.reporting_api.errors.MontaguError
import org.vaccineimpact.reporting_api.errors.UnableToParseJsonError
import org.vaccineimpact.reporting_api.errors.UnexpectedError
import spark.Request
import spark.Response
import spark.Spark as spk

class ErrorHandler
{
    private val logger = LoggerFactory.getLogger(ErrorHandler::class.java)

    init
    {
        @Suppress("RemoveExplicitTypeArguments")
        sparkException<MontaguError>(this::handleError)
        sparkException<JsonSyntaxException> { e, req, res -> handleError(UnableToParseJsonError(e), req, res) }
        sparkException<Exception> {
            e, req, res ->
            logger.error("An unhandled exception occurred", e)
            handleError(UnexpectedError(), req, res)
        }
    }

    fun handleError(error: MontaguError, req: Request, res: Response)
    {
        logger.warn("For request ${req.uri()}, a ${error::class.simpleName} occurred with the following problems: ${error.problems}")
        res.body(Serializer.instance.toJson(error.asResult()))
        res.status(error.httpStatus)
        addDefaultResponseHeaders(res)
    }

    // Just a helper to let us call Spark.exception using generic type parameters
    private inline fun <reified TException : Exception> sparkException(
            noinline handler: (exception: TException,
                               req: Request, res: Response) -> Unit)
    {
        return spark.Spark.exception(TException::class.java) {
            e, req, res ->
            handler(e as TException, req, res)
        }
    }

    companion object
    {
        fun setup() = ErrorHandler()
    }
}