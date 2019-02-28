package org.vaccineimpact.orderlyweb.controllers

import khttp.responses.Response
import org.vaccineimpact.orderlyweb.ActionContext


abstract class Controller(val context: ActionContext)
{
    protected fun passThroughResponse(response: Response): String
    {
        context.setStatusCode(response.statusCode)
        return response.text
    }

    protected val reportReadingScopes by lazy {
        context.permissions
                .filter { it.name == "reports.read" }
                .map { it.scope }
    }
}