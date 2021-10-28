package org.vaccineimpact.orderlyweb.controllers.web

import com.opencsv.CSVReader
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
import java.io.BufferedReader
import java.io.Reader
import java.net.HttpURLConnection.HTTP_OK
import java.time.Instant
import javax.servlet.MultipartConfigElement

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
        //TODO: Add helper to context: getParts(names: List<String>): Map<String, String> to read all names parts from request
        // No, needs to be cleverer than that as just want reader back for csv

        val request = context.request
        request.attribute("org.eclipse.jetty.multipartConfig", MultipartConfigElement("/temp"))
        val stream = request.raw().getPart("file").getInputStream()

        val reader = BufferedReader(stream.reader())
        /*var content: String = "not set yet"
        reader.use { reader ->
            content = reader.readText()
        }
        println("READ FILE FROM REQUEST: " + content)

        val gitstream = request.raw().getPart("git_branch").getInputStream()

        val gitreader = BufferedReader(gitstream.reader())
        var gitcontent: String = "not set yet"
        gitreader.use { reader ->
            gitcontent = reader.readText()
        }
        println("git_branch: " + gitcontent)*/

        val workflowReports = csvToWorkflowReports(reader)
        // TODO: validate against orderly reports

        return workflowReports
    }

    //TODO: move this to logic classs - rename parseWorkflowCSV
    private fun csvToWorkflowReports(reader: Reader): List<WorkflowReportWithParams>
    {
        var rows: List<Array<String>> = listOf()
        reader.use {
            CSVReader(it).use { csvReader ->
                rows = csvReader.readAll()
            }
        }

        if (rows.count() < 2)
        {
            throw BadRequest("File contains no rows")
        }

        val headers = rows[0];
        if (headers.count() < 1)
        {
           throw BadRequest("File contains no headers")
        }

        if (headers[0] != "report")
        {
            throw BadRequest("First header must be 'report'")
        }

        val columnCount = headers.count()
        val paramNames = headers.drop(1)

        return rows.mapIndexed { rowIdx, row ->
            if (row.count() != columnCount)
            {
                throw BadRequest("Row ${rowIdx + 1} should contain ${columnCount} values")
            }

            val reportName = row[0]
            val parameters = paramNames.mapIndexed { i, name ->
                name to row[i + 1]
            }.filter { it.second.isNotBlank() }.toMap()
            WorkflowReportWithParams(reportName, parameters)
        }
    }
}
