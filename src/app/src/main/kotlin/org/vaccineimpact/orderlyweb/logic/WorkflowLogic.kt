package org.vaccineimpact.orderlyweb.logic

import com.opencsv.CSVReader
import org.vaccineimpact.orderlyweb.errors.BadRequest
import org.vaccineimpact.orderlyweb.models.WorkflowReportWithParams
import java.io.Reader

class WorkflowLogic
{
    fun parseWorkflowCSV(reader: Reader): List<WorkflowReportWithParams>
    {
        var rows: List<Array<String>> = listOf()
        reader.use {
            CSVReader(it).use { csvReader -> rows = csvReader.readAll() }
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
