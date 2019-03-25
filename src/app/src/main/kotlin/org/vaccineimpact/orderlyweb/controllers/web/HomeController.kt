package org.vaccineimpact.orderlyweb.controllers.web

import org.slf4j.LoggerFactory
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.controllers.api.Template
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.Orderly
import org.vaccineimpact.orderlyweb.db.OrderlyClient
import org.vaccineimpact.orderlyweb.models.Report
import org.vaccineimpact.orderlyweb.security.WebTokenHelper
import org.vaccineimpact.orderlyweb.security.clients.JWTCookieClient
import org.vaccineimpact.orderlyweb.viewmodels.AppViewModel

class HomeController(actionContext: ActionContext,
                     private val tokenHelper: WebTokenHelper,
                     private val orderly: OrderlyClient) : Controller(actionContext)
{
    constructor(actionContext: ActionContext) : this(actionContext, WebTokenHelper.instance, Orderly())

    private val logger = LoggerFactory.getLogger(HomeController::class.java)

    data class IndexViewModel(val reports: List<Report>) : AppViewModel()

    @Template("index.ftl")
    fun index(): IndexViewModel
    {
        return IndexViewModel(orderly.getAllReports())
    }

    fun login()
    {
        var referrer = context.request.headers("referer")

        context.setCookie(JWTCookieClient.cookie,
                tokenHelper.issuer.generateBearerToken(context.userProfile.id), AppConfig())

        if (referrer == null || !referrer.contains(AppConfig()["app.url"]))
        {
            referrer = "/"
        }

        logger.info("Successful login occurred. Redirecting user to $referrer")
        context.getSparkResponse().redirect(referrer)
    }

}