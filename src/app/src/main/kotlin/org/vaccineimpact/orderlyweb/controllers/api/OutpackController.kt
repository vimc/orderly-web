package org.vaccineimpact.orderlyweb.controllers.api

import org.vaccineimpact.orderlyweb.ActionContext
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

    fun get(): Boolean
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
        return writeResponseToOutputStream(outpackServerClient.get(url, context))
    }
}
