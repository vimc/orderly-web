package org.vaccineimpact.orderlyweb.controllers.web

import com.google.gson.JsonSyntaxException
import com.google.gson.annotations.SerializedName
import org.vaccineimpact.orderlyweb.*
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.repositories.OrderlyWebWorkflowRunRepository
import org.vaccineimpact.orderlyweb.db.repositories.WorkflowRunRepository
import org.vaccineimpact.orderlyweb.errors.BadRequest
import org.vaccineimpact.orderlyweb.models.*
import org.vaccineimpact.orderlyweb.viewmodels.WorkflowRunViewModel
import java.net.HttpURLConnection.HTTP_OK
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

    fun getWorkflowRunDetails(): WorkflowRun
    {
        val key = context.params(":key")
        return workflowRunRepository.getWorkflowRunDetails(key)
        // if (key == "key1"){
        //     return WorkflowRun("name1", "key1", "email1", Instant.now(), listOf(WorkflowReportWithParams("name1a", mapOf("p1a" to "v1a"))), mapOf("p1" to "v1"), "gitBranch1", "gitCommit1")
        // } else if (key == "key2"){
        //     return WorkflowRun("name2", "key2", "email2", Instant.now(), listOf(WorkflowReportWithParams("name2a", mapOf("p2a" to "v2a"))), mapOf("p2" to "v2"), "gitBranch2", "gitCommit2")
        // } else return workflowRunRepository.getWorkflowRunDetails(key)
    }

    fun getWorkflowRunSummaries(): List<WorkflowRunSummary>
    {
        return workflowRunRepository.getWorkflowRunSummaries(
            context.queryParams("email"),
            context.queryParams("namePrefix")
        )
        // return listOf(WorkflowRunSummary("name1",  "key1", "email1", Instant.now()), WorkflowRunSummary("name2",  "key2", "email2", Instant.now()))
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
                    workflowRunRequest.reports,
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
            // val date: String
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
        val reportNames = workflowRunRepository.getWorkflowRunDetails(key).reports.map { it.name }
        return WorkflowRunStatus(
            workflowRunStatusResponse.status,
            workflowRunStatusResponse.reports.zip(reportNames) { report, reportName ->
            // WorkflowRunStatus.WorkflowRunReportStatus(reportName, report.key, report.status, report.version, report.date)
            WorkflowRunStatus.WorkflowRunReportStatus(reportName, report.key, report.status, report.version)
        })
    //     val response1 = """
    //     {
    //       "status": "success",
    //       "errors": null,
    //       "data": {
    //         "status": "running",
    //         "reports": [
    //           {
    //             "key": "preterrestrial_andeancockoftherock",
    //             "name": "report one a",
    //             "status": "error",
    //             "date": "${Instant.now()}"
    //           },
    //           {
    //             "key": "hygienic_mammoth",
    //             "name": "report two a",
    //             "status": "success",
    //             "version": "20210510-100458-8f1a9624",
    //             "date": "${Instant.now()}"
    //           },
    //           {
    //             "key": "blue_bird",
    //             "name": "report three a",
    //             "status": "running",
    //             "date": null
    //           }
    //         ]
    //       }
    //     }
    //     """
    //     val response2 = """
    //     {
    //       "status": "success",
    //       "errors": null,
    //       "data": {
    //         "status": "running",
    //         "reports": [
    //           {
    //             "key": "preterrestrial_andeancockoftherock",
    //             "name": "report one b",
    //             "status": "error",
    //             "date": "${Instant.now()}"
    //           },
    //           {
    //             "key": "hygienic_mammoth",
    //             "name": "report two b",
    //             "status": "success",
    //             "version": "20210510-100458-8f1a9624",
    //             "date": "${Instant.now()}"
    //           },
    //           {
    //             "key": "blue_bird",
    //             "name": "report three b",
    //             "status": "running",
    //             "date": null
    //           }
    //         ]
    //       }
    //     }
    //     """
    //     val response3 = """
    //     {
    //       "status": "failure"
    //     }
    //     """
    //     if (key == "key1"){
    //         return response1
    //     } else if (key == "key2"){
    //         return response2
    //     } else return response3
    }
}
