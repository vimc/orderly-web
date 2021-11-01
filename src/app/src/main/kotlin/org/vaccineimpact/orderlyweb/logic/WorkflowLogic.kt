package org.vaccineimpact.orderlyweb.logic

import com.opencsv.CSVReader
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.OrderlyServerAPI
import org.vaccineimpact.orderlyweb.errors.BadRequest
import org.vaccineimpact.orderlyweb.models.Parameter
import org.vaccineimpact.orderlyweb.models.WorkflowReportWithParams
import java.io.Reader

interface WorkflowLogic
{
    fun parseAndValidateWorkflowCSV(
        reader: Reader,
        context: ActionContext,
        orderly: OrderlyServerAPI
    ): List<WorkflowReportWithParams>
}

class OrderlyWebWorkflowLogic : WorkflowLogic
{
    override fun parseAndValidateWorkflowCSV(
        reader: Reader,
        context: ActionContext,
        orderly: OrderlyServerAPI
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

        val columnCount = headers.count()
        val paramNames = headers.drop(1)

        val errors: MutableList<String> = mutableListOf()
        val reports = rows.drop(1).mapIndexed { rowIdx, row ->
            if (row.count() != columnCount)
            {
                errors.add("Report row ${rowIdx + 1} should contain $columnCount values, ${row.count()} values found")
            }

            val reportName = row[0]
            val parameters = paramNames.mapIndexed { i, name ->
                name to row[i + 1]
            }.filter { it.second.isNotBlank() }.toMap()

            WorkflowReportWithParams(reportName, parameters)
        }

        val errorTemplate = { index: Int, msg: String -> "Report row $index: $msg" }
        errors += validateWorkflowReports(reports, orderly, context, errorTemplate)

        if (errors.isNotEmpty())
        {
            BadRequest(errors)
        }

        return reports
    }

    private fun validateWorkflowReports(
        reports: List<WorkflowReportWithParams>,
        orderly: OrderlyServerAPI,
        context: ActionContext,
        errorTemplate: (index: Int, msg: String) -> String
    ): List<String>
    {
        val runnableReports = orderly.getRunnableReportNames(context)
        val knownOrderlyReportParams: MutableMap<String, List<Parameter>> = mutableMapOf()
        val errors: MutableList<String> = mutableListOf()

        reports.forEachIndexed { index, report ->
            val reportIdx = index + 1
            if (!runnableReports.contains(report.name))
            {
                errors.add(errorTemplate(reportIdx, "report '${report.name}' not found in Orderly"))
            }
            else
            {
                if (!knownOrderlyReportParams.containsKey(report.name))
                {
                    knownOrderlyReportParams[report.name] = orderly.getReportParameters(report.name, context)
                }

                val orderlyParams = knownOrderlyReportParams[report.name]!!.associate{ it.name to it }
                val missingParameters = orderlyParams.values
                        .filter{ it.value == "" && !report.params.keys.contains(it.name) }
                missingParameters.forEach{
                    errors.add(
                            reportIdx,
                            "required parameter '${it.name}' was not provided for report '${report.name}'"
                    )
                }

                val unexpectedParameters = report.params.keys.filterNot{ orderlyParams.keys.contains(it) }
                unexpectedParameters.forEach{
                    errors.add(reportIdx, "unexpected parameter '$it' provided for report '${report.name}'")
                }
            }
        }

        return errors
    }
}
