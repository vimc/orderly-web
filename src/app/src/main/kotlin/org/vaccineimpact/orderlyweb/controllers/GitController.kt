package org.vaccineimpact.orderlyweb.controllers

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.KHttpClient
import org.vaccineimpact.orderlyweb.OrderlyServer
import org.vaccineimpact.orderlyweb.OrderlyServerAPI
import org.vaccineimpact.orderlyweb.db.AppConfig

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