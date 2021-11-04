package org.vaccineimpact.orderlyweb.logic

import com.opencsv.CSVReader
import org.vaccineimpact.orderlyweb.OrderlyServer
import org.vaccineimpact.orderlyweb.OrderlyServerAPI
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.errors.BadRequest
import org.vaccineimpact.orderlyweb.models.Parameter
import org.vaccineimpact.orderlyweb.models.WorkflowReportWithParams
import java.io.Reader

interface WorkflowLogic
{
    fun parseAndValidateWorkflowCSV(
        reader: Reader,
        branch: String?,
        commit: String?
    ): List<WorkflowReportWithParams>
}

class OrderlyWebWorkflowLogic(private val orderly: OrderlyServerAPI) : WorkflowLogic
{
    constructor() : this(OrderlyServer(AppConfig()).throwOnError())

    override fun parseAndValidateWorkflowCSV(
        reader: Reader,
        branch: String?,
        commit: String?
    ): List<WorkflowReportWithParams>
    {
        val rows = CSVReader(reader).use { it.readAll() }
        if (rows.isEmpty())
        {
            throw BadRequest("File contains no rows")
        }

        val headers = rows[0]
        if (headers[0] != "report")
        {
            throw BadRequest("First header must be 'report'")
        }

        if (rows.count() == 1)
        {
            throw BadRequest("File contains no reports")
        }

        val numCols = headers.count()

        // We expect the headers to be "report", param1, param2,...paramN
        val paramNames = headers.drop(1)

        val errors: MutableList<String> = mutableListOf()
        val errorTemplate = { index: Int, msg: String -> "Report row $index: $msg" }
        val reports = rows.drop(1).mapIndexed { rowIdx, row ->
            val numCells = row.count()
            if (numCells != numCols)
            {
                errors.add(errorTemplate(rowIdx + 1, "row should contain $numCols values, $numCells values found"))
            }

            val reportName = row[0]
            val parameters = paramNames.mapIndexed { i, name ->
                name to if (row.count() > i + 1) row[i + 1] else ""
            }.filter { it.second.isNotEmpty() }.toMap()

            WorkflowReportWithParams(reportName, parameters)
        }

        errors += validateWorkflowReports(reports, branch, commit, errorTemplate)

        if (errors.isNotEmpty())
        {
            throw BadRequest(errors)
        }

        return reports
    }

    private fun validateWorkflowReports(
        reports: List<WorkflowReportWithParams>,
        branch: String?,
        commit: String?,
        errorTemplate: (index: Int, msg: String) -> String
    ): List<String>
    {
        val runnableReports = orderly.getRunnableReportNames(mapOf("branch" to branch, "commit" to commit))
        val knownOrderlyReportParams: MutableMap<String, List<Parameter>> = mutableMapOf()
        val errors: MutableList<String> = mutableListOf()
        val getParamsQs = mapOf("commit" to commit)

        reports.forEachIndexed { index, report ->
            val reportIdx = index + 1
            if (!runnableReports.contains(report.name))
            {
                errors.add(errorTemplate(reportIdx, "report '${report.name}' not found in Orderly"))
            }
            else
            {
                var orderlyParamsList = knownOrderlyReportParams[report.name]
                if (orderlyParamsList == null)
                {
                    orderlyParamsList = orderly.getReportParameters(report.name, getParamsQs)
                    knownOrderlyReportParams[report.name] = orderlyParamsList
                }

                val orderlyParams = orderlyParamsList.associateBy { it.name }
                val missingParameters = orderlyParams.values
                        .filter{ it.value == null && !report.params.keys.contains(it.name) }
                missingParameters.forEach{
                    errors.add(
                            errorTemplate(reportIdx,
                            "required parameter '${it.name}' was not provided for report '${report.name}'")
                    )
                }

                val unexpectedParameters = report.params.keys.filterNot{ orderlyParams.keys.contains(it) }
                unexpectedParameters.forEach{
                    errors.add(
                            errorTemplate(reportIdx, "unexpected parameter '$it' provided for report '${report.name}'")
                    )
                }
            }
        }

        return errors
    }
}
