package org.vaccineimpact.orderlyweb.controllers.api

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.KHttpClient
import org.vaccineimpact.orderlyweb.OrderlyServer
import org.vaccineimpact.orderlyweb.OrderlyServerAPI
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.db.AppConfig

class GitController(context: ActionContext,
                    private val orderlyServerAPI: OrderlyServerAPI) : Controller(context)
{

    constructor(context: ActionContext) :
            this(context, OrderlyServer(AppConfig(), KHttpClient()))

    fun status(): String
    {
        val response = orderlyServerAPI.get("/v1/reports/git/status/", context)
        return passThroughResponse(response)
    }

    fun fetch(): String
    {
        val response = orderlyServerAPI.post("/v1/reports/git/fetch/", context)
        return passThroughResponse(response)
    }

    fun pull(): String
    {
        val response = orderlyServerAPI.post("/v1/reports/git/pull/", context)
        return passThroughResponse(response)
    }
}