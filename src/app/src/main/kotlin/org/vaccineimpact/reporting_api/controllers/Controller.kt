package org.vaccineimpact.reporting_api.controllers

import khttp.responses.Response
import org.vaccineimpact.reporting_api.ActionContext


abstract class Controller(val context: ActionContext)
{
    protected fun returnFromResponse(response: Response): String
    {
        context.setStatusCode(response.statusCode)
        return response.text
    }
}