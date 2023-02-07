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
                    OrderlyServerClient(AppConfig()).throwOnError()
            )

    fun pack(): Boolean
    {
        val url = "/v1/bundle/pack/${context.params(":name")}"
        val response = orderlyServerAPI.post(url, context, accept = ContentTypes.any)
        val servletResponse = context.getSparkResponse().raw()
        servletResponse.contentType = "application/zip" // TODO content type to be passed through after VIMC-4388
        servletResponse.outputStream.write(response.bytes)
        return true
    }

    fun import(): String
    {
        val url = "/v1/bundle/import"
        val response = orderlyServerAPI.post(url, context, rawRequest = true)
        return response.text
    }
}
