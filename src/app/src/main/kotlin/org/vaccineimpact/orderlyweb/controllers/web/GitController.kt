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
            this(context, OrderlyServer(AppConfig()))

    fun getCommits() : String
    {
        val branch = context.params(":branch")
        val response = orderlyServerAPI.get("/git/commits?branch=${branch}", context)
        return passThroughResponse(response)
    }

    fun fetch(): String
    {
        val successCode = 200
        val fetchResponse = orderlyServerAPI
            .throwOnError()
            .post("/v1/reports/git/fetch/", context)
        if (fetchResponse.statusCode == successCode)
        {
            val branchResponse = orderlyServerAPI
                    .throwOnError()
                    .get("/git/branches", context)
                    // NB context param will have no effect as no query string for this request
            return passThroughResponse(branchResponse)
        }
        return passThroughResponse(fetchResponse)
    }
}
