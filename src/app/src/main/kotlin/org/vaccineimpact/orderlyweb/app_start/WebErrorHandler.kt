package org.vaccineimpact.orderlyweb.app_start

import org.vaccineimpact.orderlyweb.DirectActionContext
import org.vaccineimpact.orderlyweb.errors.FailedLoginError
import org.vaccineimpact.orderlyweb.errors.OrderlyWebError
import org.vaccineimpact.orderlyweb.errors.RouteNotFound
import org.vaccineimpact.orderlyweb.viewmodels.FailedLoginViewModel
import org.vaccineimpact.orderlyweb.viewmodels.PageNotFoundViewModel
import org.vaccineimpact.orderlyweb.viewmodels.ServerErrorViewModel
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

        val modelAndView = when (error)
        {
            is RouteNotFound ->
                ModelAndView(PageNotFoundViewModel(context), "404.ftl")

            is FailedLoginError ->
                ModelAndView(FailedLoginViewModel(context), "401.ftl")

            else ->
                ModelAndView(ServerErrorViewModel(error, context), "500.ftl")
        }

        res.body(freeMarkerEngine.render(modelAndView))
    }
}
