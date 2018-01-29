package org.vaccineimpact.reporting_api.controllers

import org.vaccineimpact.reporting_api.ActionContext
import org.vaccineimpact.reporting_api.KHttpClient
import org.vaccineimpact.reporting_api.OrderlyServer
import org.vaccineimpact.reporting_api.OrderlyServerAPI
import org.vaccineimpact.reporting_api.db.AppConfig

class GitController(context: ActionContext,
                    private val orderlyServerAPI: OrderlyServerAPI) : Controller(context)
{

    constructor(context: ActionContext) :
            this(context, OrderlyServer(AppConfig(), KHttpClient()))

    fun status(): String
    {
        val response = orderlyServerAPI.get("/reports/git/status/", context)
        return passThroughResponse(response)
    }

    fun fetch(): String
    {
        val response = orderlyServerAPI.post("/reports/git/fetch/", context)
        return passThroughResponse(response)
    }

    fun pull(): String
    {
        val response = orderlyServerAPI.post("/reports/git/pull/", context)
        return passThroughResponse(response)
    }
}