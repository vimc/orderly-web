package org.vaccineimpact.orderlyweb.controllers.web

import com.google.gson.annotations.SerializedName
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.OrderlyServer
import org.vaccineimpact.orderlyweb.OrderlyServerAPI
import org.vaccineimpact.orderlyweb.Serializer
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.repositories.OrderlyWebWorkflowRunRepository
import org.vaccineimpact.orderlyweb.db.repositories.WorkflowRunRepository
import org.vaccineimpact.orderlyweb.models.WorkflowRun
import org.vaccineimpact.orderlyweb.models.WorkflowRunRequest
import org.vaccineimpact.orderlyweb.models.WorkflowRunSummary
import org.vaccineimpact.orderlyweb.viewmodels.WorkflowRunViewModel
import java.time.Instant

class WorkflowRunController(
    context: ActionContext,
    private val workflowRunRepository: WorkflowRunRepository,
    private val orderlyServerAPI: OrderlyServerAPI
) : Controller(context)
{
    constructor(context: ActionContext) : this(
        context,
        OrderlyWebWorkflowRunRepository(),
        OrderlyServer(AppConfig()).throwOnError()
    )

    @Template("run-workflow-page.ftl")
    fun getRunWorkflow(): WorkflowRunViewModel
    {
        return WorkflowRunViewModel(context)
    }

    fun getWorkflowRunSummaries(): List<WorkflowRunSummary>
    {
        return workflowRunRepository.getWorkflowRunSummaries(
            context.queryParams("email"),
            context.queryParams("namePrefix")
        )
    }

    internal data class WorkflowRunResponse(
        @SerializedName(value = "workflow_key")
        val key: String,
        val reports: List<String>
    )

    @Suppress("UnsafeCallOnNullableType")
    fun createWorkflowRun(): String
    {
        val workflowRunRequest =
            Serializer.instance.gson.fromJson(context.getRequestBody(), WorkflowRunRequest::class.java)

        val body = Serializer.instance.gson.toJson(
            listOfNotNull(
                ("changelog" to workflowRunRequest.changelog).takeIf { it.second != null },
                ("ref" to workflowRunRequest.gitCommit).takeIf { it.second != null },
                "reports" to workflowRunRequest.reports.map {
                    mapOf(
                        "name" to it.name,
                        "params" to it.params,
                        "instance" to workflowRunRequest.instances // TODO
                    )
                }
            ).toMap()
        )
        val response =
            orderlyServerAPI.post(
                "/v1/workflow/run/",
                body,
                emptyMap()
            )
        val workflowRun = response.data(WorkflowRunResponse::class.java)
        workflowRunRepository.addWorkflowRun(
            WorkflowRun(
                workflowRunRequest.name,
                workflowRun.key,
                context.userProfile!!.id,
                Instant.now(),
                workflowRunRequest.reports,
                workflowRunRequest.instances,
                workflowRunRequest.gitBranch,
                workflowRunRequest.gitCommit
            )
        )
        return passThroughResponse(response)
    }
}
