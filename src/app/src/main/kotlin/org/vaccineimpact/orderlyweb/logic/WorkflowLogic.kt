package org.vaccineimpact.orderlyweb.logic

import com.opencsv.CSVReader
import org.vaccineimpact.orderlyweb.errors.BadRequest
import org.vaccineimpact.orderlyweb.models.WorkflowReportWithParams
import java.io.Reader

interface WorkflowLogic
{
    fun parseWorkflowCSV(reader: Reader): List<WorkflowReportWithParams>
}

class OrderlyWebWorkflowLogic : WorkflowLogic
{
    override fun parseWorkflowCSV(reader: Reader): List<WorkflowReportWithParams>
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

        return rows.drop(1).mapIndexed { rowIdx, row ->
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
    }
}
