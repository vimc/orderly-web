package org.vaccineimpact.orderlyweb.controllers.api

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.OrderlyServerClient
import org.vaccineimpact.orderlyweb.OrderlyServerAPI
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.Config

class QueueController(
    context: ActionContext,
    config: Config,
    private val orderlyServerAPI: OrderlyServerAPI
) : Controller(context, config)
{
    @Suppress("unused")
    constructor(context: ActionContext) :
            this(
                    context,
                    AppConfig(),
                    OrderlyServerClient(AppConfig()).throwOnError()
            )

    fun getStatus(): String
    {
        val url = "/v1/queue/status/"
        val response = orderlyServerAPI.get(url, context)
        return passThroughResponse(response)
    }
}
