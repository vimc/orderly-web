package org.vaccineimpact.orderlyweb.controllers

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.PorcelainResponse
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.Config

abstract class Controller(val context: ActionContext, val appConfig: Config = AppConfig())
{
    protected fun passThroughResponse(response: PorcelainResponse): String
    {
        context.setStatusCode(response.statusCode)
        return response.text
    }

    protected fun writeResponseToOutputStream(response: PorcelainResponse): Boolean
    {
        context.setStatusCode(response.statusCode)
        val servletResponse = context.getSparkResponse().raw()
        response.headers.map { servletResponse.setHeader(it.first, it.second) }
        servletResponse.outputStream.write(response.bytes)
        return true
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
