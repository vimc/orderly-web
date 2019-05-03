package org.vaccineimpact.orderlyweb.app_start

import org.vaccineimpact.orderlyweb.DirectActionContext
import org.vaccineimpact.orderlyweb.errors.FailedLoginError
import org.vaccineimpact.orderlyweb.errors.OrderlyWebError
import org.vaccineimpact.orderlyweb.errors.RouteNotFound
import org.vaccineimpact.orderlyweb.viewmodels.AppViewModel
import spark.ModelAndView
import spark.Request
import spark.Response
import spark.template.freemarker.FreeMarkerEngine

class WebErrorHandler(private val freeMarkerEngine: FreeMarkerEngine)
{
    fun handleError(error: OrderlyWebError, req: Request, res: Response)
    {
        val context = DirectActionContext(req, res)
        res.type("text/html")
        res.status(error.httpStatus)

        val template = when (error)
        {
            is FailedLoginError -> "401.ftl"
            is RouteNotFound -> "404.ftl"
            else -> "error.ftl"
        }
        res.body(freeMarkerEngine.render(
                ModelAndView(ErrorViewModel(error, context), template)
        ))
    }

    open class ErrorViewModel(error: OrderlyWebError, context: DirectActionContext) : AppViewModel(context)
    {
        open val errors = error.problems
    }
}
