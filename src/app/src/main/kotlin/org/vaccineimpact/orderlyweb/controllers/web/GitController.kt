package org.vaccineimpact.orderlyweb.controllers.web

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.OrderlyServerAPI
import org.vaccineimpact.orderlyweb.OrderlyServerClient
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.db.AppConfig
import javax.net.ssl.HttpsURLConnection.HTTP_OK

class GitController(
        context: ActionContext,
        private val orderlyServerAPI: OrderlyServerAPI
) : Controller(context)
{
    constructor(context: ActionContext) :
            this(context, OrderlyServerClient(AppConfig()))

    fun getCommits(): String
    {
        val branch = context.params(":branch")
        val response = orderlyServerAPI.get("/git/commits?branch=$branch", context)
        return passThroughResponse(response)
    }

    fun fetch(): String
    {
        val fetchResponse = orderlyServerAPI
                .post("/v1/reports/git/fetch/", context)
        if (fetchResponse.statusCode == HTTP_OK)
        {
            val branchResponse = orderlyServerAPI
                    .get("/git/branches", context)
            // NB context param will have no effect as no query string for this request
            return passThroughResponse(branchResponse)
        }
        return passThroughResponse(fetchResponse)
    }
}
