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

    fun getWorkflowSummary(): WorkflowSummary
    {
        val response = orderlyServerAPI.post(
                "/v1/workflow/summary/",
                context.getRequestBody(),
                emptyMap()
        )
        val summary = response.data(WorkflowSummary::class.java)
        val reports = summary.reports.map { report ->
            val defaultParams = orderlyServerAPI.getReportParameters(report.name, context.queryParams())
                    .associate { it.name to it.value }
            report.copy(
                    defaultParamList = report.params?.filter { defaultParams[it.key] == it.value }?.map {
                        Parameter(it.key, it.value)
                    },
                    paramList = report.params?.filter { defaultParams[it.key] != it.value }?.map {
                        Parameter(it.key, it.value)
                    })
        }
        return WorkflowSummary(reports, summary.ref, summary.missingDependencies)
    }

    internal data class WorkflowQueuedReport(val name: String,
                                             val key: String,
                                             @SerializedName(value = "execution_order")
                                             val executionOrder: Int,
                                             val params: Map<String, String>?)

    internal data class WorkflowRunResponse(
            @SerializedName(value = "workflow_key")
            val key: String,
            val reports: List<WorkflowQueuedReport>
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
                                    // TODO remove this in favour of passing instances to orderly.server
                                    // see VIMC-4561
                                    ("instance" to workflowRunRequest.instances?.values?.elementAtOrNull(0))
                                            .takeIf {
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
                            workflowRun.reports.map {
                                WorkflowRunReport(
                                        workflowRun.key,
                                        it.key,
                                        it.executionOrder,
                                        it.name,
                                        it.params ?: mapOf()
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
                runWorkflow.reports.sortedBy { it.executionOrder }.map { report ->
                    @Suppress("UnsafeCallOnNullableType")
                    val status = workflowRunStatusResponse.reports.find { it.key == report.key }!!
                    WorkflowRunStatus.WorkflowRunReportStatus(
                            report.report,
                            report.key,
                            status.status,
                            status.version
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
