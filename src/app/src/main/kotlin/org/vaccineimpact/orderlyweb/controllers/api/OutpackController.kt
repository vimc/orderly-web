package org.vaccineimpact.orderlyweb.controllers.api

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.ContentTypes
import org.vaccineimpact.orderlyweb.OutpackServerClient
import org.vaccineimpact.orderlyweb.PorcelainAPI
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.db.AppConfig

class OutpackController(
        context: ActionContext,
        private val outpackServerClient: PorcelainAPI
) : Controller(context)
{
    constructor(context: ActionContext) : this(
            context,
            OutpackServerClient(AppConfig())
    )

    fun get(): String
    {
        val splat = context.splat()
        val url = if (splat?.isNotEmpty() == true)
        {
            "/${splat[0]}"
        }
        else
        {
            "/"
        }
        return passThroughResponse(outpackServerClient.get(url, context))
    }

    fun getFile(): Boolean
    {
        val url = "/file/${context.params(":hash")}"
        val response = outpackServerClient
                .throwOnError()
                .get(url, context, transformResponse = false)
        val servletResponse = context.getSparkResponse().raw()
        servletResponse.contentType = ContentTypes.binarydata
        servletResponse.setContentLength(response.headers["content-length"]!!.toInt())
        servletResponse.setHeader("content-disposition", response.headers["content-disposition"])
        servletResponse.outputStream.write(response.bytes)
        return true
    }
}
