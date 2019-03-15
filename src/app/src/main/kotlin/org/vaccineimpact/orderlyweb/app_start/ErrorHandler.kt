package org.vaccineimpact.orderlyweb.app_start

import com.google.gson.JsonSyntaxException
import org.slf4j.LoggerFactory
import org.vaccineimpact.orderlyweb.ContentTypes
import org.vaccineimpact.orderlyweb.Serializer
import org.vaccineimpact.orderlyweb.addDefaultResponseHeaders
import org.vaccineimpact.orderlyweb.errors.OrderlyWebError
import org.vaccineimpact.orderlyweb.errors.UnableToParseJsonError
import org.vaccineimpact.orderlyweb.errors.UnexpectedError
import spark.Request
import spark.Response
import java.lang.reflect.InvocationTargetException
import spark.Spark as spk

class ErrorHandler
{
    private val logger = LoggerFactory.getLogger(ErrorHandler::class.java)

    init
    {
        @Suppress("RemoveExplicitTypeArguments")
        sparkException<InvocationTargetException>(this::handleInvocationError)
        sparkException<OrderlyWebError>(this::handleError)
        sparkException<JsonSyntaxException> { e, req, res -> handleError(UnableToParseJsonError(e), req, res) }
        sparkException<Exception> { e, req, res ->
            logger.error("An unhandled exception occurred", e)
            handleError(UnexpectedError(), req, res)
        }
    }

    // because routes are configured using reflection,
    // all controller errors appear as InvocationTargetExceptions
    fun handleInvocationError(error: InvocationTargetException, req: Request, res: Response)
    {

        val cause = error.cause!!

        when (cause)
        {
            is OrderlyWebError -> this.handleError(cause, req, res)
            is JsonSyntaxException -> this.handleError(UnableToParseJsonError(cause), req, res)
            else ->
            {
                logger.error("An unhandled exception occurred", cause)
                handleError(UnexpectedError(), req, res)
            }
        }
    }

    fun handleError(error: OrderlyWebError, req: Request, res: Response)
    {
        logger.warn("For request ${req.uri()}, a ${error::class.simpleName} occurred with the following problems: ${error.problems}")
        res.body(Serializer.instance.toJson(error.asResult()))
        res.status(error.httpStatus)
        addDefaultResponseHeaders(res.raw(), "${ContentTypes.json}; charset=utf-8")
    }

    // Just a helper to let us call Spark.exception using generic type parameters
    private inline fun <reified TException : Exception> sparkException(
            noinline handler: (exception: TException,
                               req: Request, res: Response) -> Unit)
    {
        return spark.Spark.exception(TException::class.java) { e, req, res ->
            handler(e as TException, req, res)
        }
    }

    companion object
    {
        fun setup() = ErrorHandler()
    }
}