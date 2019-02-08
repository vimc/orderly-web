package org.vaccineimpact.reporting_api.controllers

import khttp.responses.Response
import org.vaccineimpact.api.models.Scope
import org.vaccineimpact.api.models.encompass
import org.vaccineimpact.api.models.permissions.PermissionSet
import org.vaccineimpact.reporting_api.ActionContext
import org.vaccineimpact.reporting_api.errors.MissingRequiredPermissionError


abstract class Controller(val context: ActionContext)
{
    protected fun passThroughResponse(response: Response): String
    {
        context.setStatusCode(response.statusCode)
        return response.text
    }

    protected val reportReadingScopes = context.permissions
            .filter { it.name == "reports.read" }
            .map { it.scope }

    protected fun checkCanReadReport(context: ActionContext)
    {
       if (!reportReadingScopes.encompass(Scope.Specific("report", context.params(":name"))))
       {
           throw MissingRequiredPermissionError(PermissionSet("*/reports.read"))
       }
    }

    protected val isReportReviewer = context.permissions.names.contains("reports.review")
}