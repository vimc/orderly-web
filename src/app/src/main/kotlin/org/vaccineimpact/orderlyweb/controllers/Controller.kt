package org.vaccineimpact.orderlyweb.controllers

import org.vaccineimpact.orderlyweb.OrderlyServerResponse
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.Config

abstract class Controller(val context: ActionContext, val appConfig: Config = AppConfig())
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
}
