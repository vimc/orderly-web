package org.vaccineimpact.orderlyweb.controllers

import khttp.responses.Response
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.encompass
import org.vaccineimpact.orderlyweb.models.permissions.PermissionSet
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.Config


abstract class Controller(val context: ActionContext, val appConfig: Config = AppConfig())
{
    protected fun passThroughResponse(response: Response): String
    {
        context.setStatusCode(response.statusCode)
        return response.text
    }

    protected fun okayResponse() = "OK"

    private val reportReadingScopes by lazy {
        context.permissions
                .filter { it.name == "reports.read" }
                .map { it.scope }
    }

    protected fun canReadReports(): Boolean
    {
        return if (appConfig.authorizationEnabled)
        {
            reportReadingScopes.any()
        }
        else
        {
            true
        }
    }

    protected fun canReadReport(reportName: String): Boolean
    {
        return if (appConfig.authorizationEnabled)
        {
            reportReadingScopes.encompass(Scope.Specific("report", reportName))
        }
        else
        {
            true
        }
    }

}