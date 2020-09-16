package org.vaccineimpact.orderlyweb.controllers.web

import org.vaccineimpact.orderlyweb.*
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.models.GitCommit
import java.text.SimpleDateFormat

class GitController(context: ActionContext,
                    private val orderlyServerAPI: OrderlyServerAPI): Controller(context)
{
    constructor(context: ActionContext) :
            this(context, OrderlyServer(AppConfig(), KHttpClient()))

    fun getCommits() : String
    {
        val branch = context.params(":branch")
        val response = orderlyServerAPI.get("/git/commits?branch=${branch}", context)
        return passThroughResponse(response)
    }
}
