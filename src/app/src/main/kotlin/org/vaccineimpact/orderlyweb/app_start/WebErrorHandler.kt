package org.vaccineimpact.orderlyweb.app_start

import org.vaccineimpact.orderlyweb.DirectActionContext
import org.vaccineimpact.orderlyweb.errors.FailedLoginError
import org.vaccineimpact.orderlyweb.errors.OrderlyWebError
import org.vaccineimpact.orderlyweb.errors.RouteNotFound
import org.vaccineimpact.orderlyweb.viewmodels.ErrorViewModel
import org.vaccineimpact.orderlyweb.viewmodels.ServerErrorViewModel
import spark.ModelAndView
import spark.Request
import spark.Response
import spark.TemplateEngine

class WebErrorHandler(private val templateEngine: TemplateEngine)
{
    fun handleError(error: OrderlyWebError, req: Request, res: Response)
    {
        val context = DirectActionContext(req, res)
        res.type("text/html")
        res.status(error.httpStatus)

        val modelAndView = when (error)
        {
            is RouteNotFound ->
                ModelAndView(ErrorViewModel("Page not found", context), "404.ftl")

            is FailedLoginError ->
                ModelAndView(ErrorViewModel("Login failed", context), "401.ftl")

            else ->
                ModelAndView(ServerErrorViewModel(error, context), "500.ftl")
        }

        res.body(templateEngine.render(modelAndView))
    }
}
