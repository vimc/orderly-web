package org.vaccineimpact.reporting_api.controllers

import org.vaccineimpact.reporting_api.*
import org.vaccineimpact.reporting_api.db.Config

class GitController(context: ActionContext,
                    val orderlyServerAPI: OrderlyServerAPI) : Controller(context)
{

    constructor(context: ActionContext) :
            this(context, OrderlyServer(Config, KHttpClient()))

    fun status(): String
    {
        val response = orderlyServerAPI.get("/reports/git/status/", context)
        return returnFromResponse(response)
    }

    fun fetch(): String
    {
        val response = orderlyServerAPI.post("/reports/git/fetch/", context)
        return returnFromResponse(response)
    }

    fun pull(): String
    {
        val response = orderlyServerAPI.post("/reports/git/pull/", context)
        return returnFromResponse(response)
    }
}