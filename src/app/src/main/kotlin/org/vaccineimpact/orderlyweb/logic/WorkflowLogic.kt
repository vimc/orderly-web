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
    fun parseAndValidateWorkflowCSV(reader: Reader, context: ActionContext, orderly: OrderlyServerAPI): List<WorkflowReportWithParams>
}

class OrderlyWebWorkflowLogic : WorkflowLogic
{
    override fun parseAndValidateWorkflowCSV(
            reader: Reader,
            context: ActionContext,
            orderly: OrderlyServerAPI): List<WorkflowReportWithParams>
    {
        var rows: List<Array<String>> = listOf()
        reader.use {
            CSVReader(it).use { csvReader -> rows = csvReader.readAll() }
        }

        if (rows.count() == 0)
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

        var errors: List<String> = listOf()
        val reports = rows.drop(1).mapIndexed { rowIdx, row ->
            if (row.count() != columnCount)
            {
                throw BadRequest(
                        "Report row ${rowIdx + 1} should contain $columnCount values, ${row.count()} values found")
            }

            val reportName = row[0]
            val parameters = paramNames.mapIndexed { i, name ->
                name to row[i + 1]
            }.filter { it.second.isNotBlank() }.toMap()

            WorkflowReportWithParams(reportName, parameters)
        }

        errors+= validateWorkflowReports(reports, orderly, context)

        if (errors.count() > 0)
        {
            BadRequest("TODO")
        }

        return reports
    }

    private fun validateWorkflowReports(
        reports: List<WorkflowReportWithParams>,
        orderly: OrderlyServerAPI,
        context: ActionContext
    ): List<String>
    {
        val runnableReports = orderly.getRunnableReportNames(context)
        val knownOrderlyReportParams: Map<String, List<Parameter>> = mapOf()
    }
}
