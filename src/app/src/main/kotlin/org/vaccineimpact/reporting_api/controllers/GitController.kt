package org.vaccineimpact.reporting_api.controllers

import org.vaccineimpact.reporting_api.*
import org.vaccineimpact.reporting_api.db.AppConfig

class GitController(context: ActionContext,
                    private val orderlyServerAPI: OrderlyServerAPI) : Controller(context)
{

    constructor(context: ActionContext) :
            this(context, OrderlyServer(AppConfig(), KHttpClient()))

    fun status(): String
    {
        val response = orderlyServerAPI.get("/reports/git/status/", context)
        return returnResponse(response)
    }

    fun fetch(): String
    {
        val response = orderlyServerAPI.post("/reports/git/fetch/", context)
        return returnResponse(response)
    }

    fun pull(): String
    {
        val response = orderlyServerAPI.post("/reports/git/pull/", context)
        return returnResponse(response)
    }
}