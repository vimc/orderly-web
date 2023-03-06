package org.vaccineimpact.orderlyweb.controllers.api

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.ContentTypes
import org.vaccineimpact.orderlyweb.OrderlyServerAPI
import org.vaccineimpact.orderlyweb.OrderlyServerClient
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.Config

class BundleController(
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
                    OrderlyServerClient(AppConfig())
            )

    fun pack(): Boolean
    {
        val url = "/v1/bundle/pack/${context.params(":name")}"
        val response = orderlyServerAPI.post(url, context, accept = ContentTypes.any)
        return writeResponseToOutputStream(response)
    }

    fun import(): String
    {
        val url = "/v1/bundle/import"
        val response = orderlyServerAPI.post(url, context, rawRequest = true)
        return passThroughResponse(response)
    }
}
