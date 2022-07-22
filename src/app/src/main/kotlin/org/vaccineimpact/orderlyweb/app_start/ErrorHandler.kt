package org.vaccineimpact.orderlyweb.app_start

import com.google.gson.JsonSyntaxException
import org.slf4j.LoggerFactory
import org.vaccineimpact.orderlyweb.errors.OrderlyWebError
import org.vaccineimpact.orderlyweb.errors.UnableToParseJsonError
import org.vaccineimpact.orderlyweb.errors.UnexpectedError
import spark.Request
import spark.Response
import spark.TemplateEngine
import java.lang.reflect.InvocationTargetException
import spark.Spark as spk

class ErrorHandler(templateEngine: TemplateEngine,
                   private val webErrorHandler: ResponseErrorHandler = WebResponseErrorHandler(templateEngine),
                   private val apiErrorHandler: ResponseErrorHandler = APIResponseErrorHandler())
{
    private val logger = LoggerFactory.getLogger(ErrorHandler::class.java)

    init
    {
        @Suppress("RemoveExplicitTypeArguments")
        sparkException<InvocationTargetException>(this::handleInvocationError)
        sparkException<OrderlyWebError>(this::handleError)
        sparkException<JsonSyntaxException> { e, req, res -> handleError(UnableToParseJsonError(e), req, res) }
        sparkException<RuntimeException> { e, req, res ->
            // pac4j throws a RuntimeException if errors are thrown during the Authentication process
            // If the cause is an OrderlyWebError we want that to bubble up
            if (e.cause is OrderlyWebError)
            {
                handleError(e.cause as OrderlyWebError, req, res)
            }
            else
            {
                logger.error("An unhandled exception occurred", e)
                handleError(UnexpectedError(), req, res)
            }
        }
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
        val uri = req.uri()
        val errName = error::class.simpleName
        logger.warn("For request ${uri}, a ${errName} occurred with the following problems: ${error.problems}")

        if (req.pathInfo().startsWith(Router.apiUrlBase) || req.headers("Accept").contains("application/json"))
        {
            apiErrorHandler.handleError(error, req, res)
        }
        else
        {
            webErrorHandler.handleError(error, req, res)
        }
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
}
