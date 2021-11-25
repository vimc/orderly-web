package org.vaccineimpact.orderlyweb.controllers.web

import com.google.gson.JsonSyntaxException
import com.google.gson.annotations.SerializedName
import org.vaccineimpact.orderlyweb.*
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.repositories.OrderlyWebWorkflowRunRepository
import org.vaccineimpact.orderlyweb.db.repositories.WorkflowRunRepository
import org.vaccineimpact.orderlyweb.errors.BadRequest
import org.vaccineimpact.orderlyweb.logic.OrderlyWebWorkflowLogic
import org.vaccineimpact.orderlyweb.logic.WorkflowLogic
import org.vaccineimpact.orderlyweb.models.*
import org.vaccineimpact.orderlyweb.viewmodels.WorkflowRunViewModel
import java.net.HttpURLConnection.HTTP_OK
import java.time.Instant
import com.google.gson.Gson

class WorkflowRunController(
    context: ActionContext,
    private val workflowRunRepository: WorkflowRunRepository,
    private val orderlyServerAPI: OrderlyServerAPI,
    private val workflowLogic: WorkflowLogic
) : Controller(context)
{
    constructor(context: ActionContext) : this(
        context,
        OrderlyWebWorkflowRunRepository(),
        OrderlyServer(AppConfig()).throwOnError(),
        OrderlyWebWorkflowLogic()
    )

    @Template("run-workflow-page.ftl")
    fun getRunWorkflow(): WorkflowRunViewModel
    {
        return WorkflowRunViewModel(context)
    }

    fun getWorkflowRunDetails(): WorkflowRun
    {
        val key = context.params(":key")
        return workflowRunRepository.getWorkflowRunDetails(key)
    }

    fun getWorkflowRunSummaries(): List<WorkflowRunSummary>
    {
        return workflowRunRepository.getWorkflowRunSummaries(
            context.queryParams("email"),
            context.queryParams("namePrefix")
        )
    }

    fun getWorkflowRunSummary(): String
    {
        val key = context.params(":key")
        val report = mapOf("name" to "example", "instance" to "production", "params" to mapOf("nmin" to 1), "depends_on" to listOf("missing"))
        val response = mapOf("reports" to listOf(report), "ref" to "18f6c5267c08bf017b521a21493771c6d3e774a5", "missing_dependencies" to mapOf("example" to listOf("missing")))
        // val response = mapOf("status" to "success", "errors" to null, "data" to data)
        // return Gson().toJson(response)
        val report1 = WorkflowRunSummaryPageReport("example","production", mapOf("nmin" to "1"), listOf("missing"))
        return Serializer.instance.gson.toJson(WorkflowRunSummaryPage(listOf(report1), mapOf("example" to listOf("missing")), "18f6c5267c08bf017b521a21493771c6d3e774a5"))
    }

    internal data class WorkflowRunResponse(
        @SerializedName(value = "workflow_key")
        val key: String,
        val reports: List<String>
    )

    fun createWorkflowRun(): String
    {
        val workflowRunRequestJson = context.getRequestBody()

        val workflowRunRequest = try
        {
            Serializer.instance.gson.fromJson(workflowRunRequestJson, WorkflowRunRequest::class.java)
        }
        catch (e: JsonSyntaxException)
        {
            throw BadRequest("Invalid workflow description: ${e.message}")
        }

        val body = Serializer.instance.gson.toJson(
            listOfNotNull(
                ("changelog" to workflowRunRequest.changelog).takeIf { it.second != null },
                ("ref" to workflowRunRequest.gitCommit).takeIf { it.second != null },
                "reports" to workflowRunRequest.reports.map { report ->
                    listOfNotNull(
                        "name" to report.name,
                        "params" to report.params,
                        // TODO remove this in favour of passing instances itself to orderly.server - see VIMC-4561
                        ("instance" to workflowRunRequest.instances?.values?.elementAtOrNull(0)).takeIf {
                            it.second != null
                        }
                    ).toMap()
                }
            ).toMap()
        )
        val response = orderlyServerAPI.post(
            "/v1/workflow/run/",
            body,
            emptyMap()
        )
        if (response.statusCode == HTTP_OK)
        {
            val workflowRun = response.data(WorkflowRunResponse::class.java)

            workflowRunRepository.addWorkflowRun(
                WorkflowRun(
                    workflowRunRequest.name,
                    workflowRun.key,
                    @Suppress("UnsafeCallOnNullableType")
                    context.userProfile!!.id,
                    Instant.now(),
                    workflowRunRequest.reports.zip(workflowRun.reports) { report, reportKey ->
                        WorkflowRunReport(
                            workflowRun.key,
                            reportKey,
                            report.name,
                            report.params
                        )
                    },
                    workflowRunRequest.instances ?: emptyMap(),
                    workflowRunRequest.gitBranch,
                    workflowRunRequest.gitCommit
                )
            )
        }
        return passThroughResponse(response)
    }

    @NoCoverage
    internal data class WorkflowRunStatusResponse(
        @SerializedName(value = "workflow_key")
        val key: String,
        val status: String,
        val reports: List<WorkflowRunStatusResponseReport>
    )
    {
        @NoCoverage
        data class WorkflowRunStatusResponseReport(
            val key: String,
            val status: String,
            val version: String?
        )
    }

    fun getWorkflowRunStatus(): WorkflowRunStatus
    {
        val key = context.params(":key")
        val response = orderlyServerAPI
            .throwOnError()
            .get("/v1/workflow/$key/status/", emptyMap())
        val workflowRunStatusResponse = response.data(WorkflowRunStatusResponse::class.java)
        workflowRunRepository.updateWorkflowRun(key, workflowRunStatusResponse.status)

        val runWorkflow = workflowRunRepository.getWorkflowRunDetails(key)

        return WorkflowRunStatus(
            workflowRunStatusResponse.status,
            workflowRunStatusResponse.reports.map { report ->
                WorkflowRunStatus.WorkflowRunReportStatus(
                    @Suppress("UnsafeCallOnNullableType")
                    runWorkflow.reports.find{it.key == report.key}!!.report,
                    report.key,
                    report.status,
                    report.version
                )
            })
    }

    fun validateWorkflow(): List<WorkflowReportWithParams>
    {
        val reader = context.getPartReader("file")
        val branch = context.queryParams("branch")
        val commit = context.queryParams("commit")

        return workflowLogic.parseAndValidateWorkflowCSV(reader, branch, commit)
    }
}
