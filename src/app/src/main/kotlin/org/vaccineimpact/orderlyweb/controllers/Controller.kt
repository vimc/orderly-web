package org.vaccineimpact.orderlyweb.controllers

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.OrderlyServer
import org.vaccineimpact.orderlyweb.OrderlyServerAPI
import org.vaccineimpact.orderlyweb.OrderlyServerResponse
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.Config
import org.vaccineimpact.orderlyweb.models.Artefact

abstract class Controller(val context: ActionContext,
                          val appConfig: Config = AppConfig(),
                          private val orderlyApI: OrderlyServerAPI = OrderlyServer(AppConfig())
)
{
    protected fun passThroughResponse(response: OrderlyServerResponse): String
    {
        context.setStatusCode(response.statusCode)
        return response.text
    }

    protected fun okayResponse() = "OK"

    protected fun canReadReports(): Boolean
    {
        return if (appConfig.authorizationEnabled)
        {
            context.isGlobalReader() || context.reportReadingScopes.any()
        }
        else
        {
            true
        }
    }

    protected fun getArtefacts(version: String): List<Artefact>
    {
        val response = orderlyApI.throwOnError().get("/v1/report/version/${version}/artefacts", emptyMap())
        return response.listData(Artefact::class.java)
    }
}
