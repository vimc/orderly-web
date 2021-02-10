package org.vaccineimpact.orderlyweb.controllers.api

import com.google.gson.Gson
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.OrderlyServer
import org.vaccineimpact.orderlyweb.OrderlyServerAPI
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.Config
import org.vaccineimpact.orderlyweb.db.repositories.OrderlyWebReportRunRepository
import org.vaccineimpact.orderlyweb.db.repositories.ReportRunRepository
import java.time.Instant

data class ReportRun(val name: String, val key: String, val path: String)

class ReportRunController(
    context: ActionContext,
    private val reportRunRepository: ReportRunRepository,
    private val orderlyServerAPI: OrderlyServerAPI,
    config: Config
) : Controller(context, config)
{
    constructor(context: ActionContext) :
            this(
                context,
                OrderlyWebReportRunRepository(),
                OrderlyServer(AppConfig()).throwOnError(),
                AppConfig()
            )

    fun run(): String
    {
        val name = context.params(":name")

        val instances = context.postData<Map<String, String>>()["instances"] ?: emptyMap()
        val params = context.postData<Map<String, String>>()["params"] ?: emptyMap()
        val gitBranch = context.postData<String>()["gitBranch"]
        val gitCommit = context.postData<String>()["gitCommit"]

        val response =
            orderlyServerAPI.post(
                "/v1/reports/$name/run/",
                Gson().toJson(params),
                listOf(
                    "ref" to gitCommit,
                    // TODO remove this in favour of passing instances itself to orderly.server - see VIMC-4561
                    "instance" to instances.values.elementAtOrNull(0)
                ).mapNotNull { p -> p.second?.let { p } }.toMap()
            )
        val reportRun = response.data(ReportRun::class.java)
        reportRunRepository.addReportRun(
            reportRun.key,
            @Suppress("UnsafeCallOnNullableType")
            context.userProfile!!.id,
            Instant.now(),
            reportRun.name,
            instances,
            params,
            gitBranch,
            gitCommit
        )
        return passThroughResponse(response)
    }

    fun status(): String
    {
        val key = context.params(":key")
        val response = orderlyServerAPI.get("/v1/reports/$key/status/", context)
        return passThroughResponse(response)
    }

    fun kill(): String
    {
        val key = context.params(":key")
        val response = orderlyServerAPI.delete("/v1/reports/$key/kill/", context)
        return passThroughResponse(response)
    }
}
