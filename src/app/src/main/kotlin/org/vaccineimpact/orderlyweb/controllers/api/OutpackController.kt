package org.vaccineimpact.orderlyweb.controllers.api

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.OutpackServerClient
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.db.AppConfig

class OutpackController(
        context: ActionContext,
        private val outpackServerClient: OutpackServerClient
) : Controller(context)
{
    constructor(context: ActionContext) : this(
            context,
            OutpackServerClient(AppConfig())
    )

    fun index(): String
    {
        return passThroughResponse(outpackServerClient.get("/", context))
    }
}