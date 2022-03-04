package org.vaccineimpact.orderlyweb.controllers

import org.vaccineimpact.orderlyweb.*
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.Config
import org.vaccineimpact.orderlyweb.models.Artefact

abstract class Controller(val context: ActionContext,
                          val appConfig: Config = AppConfig(),
                          private val orderlyApI: OrderlyServerAPI = OrderlyServer(AppConfig()))
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
        val response = orderlyApI.get("/v1/report/version/${version}/artefacts", emptyMap())
        println(response.listData(Artefact::class.java))
        return response.listData(Artefact::class.java)
    }
}
