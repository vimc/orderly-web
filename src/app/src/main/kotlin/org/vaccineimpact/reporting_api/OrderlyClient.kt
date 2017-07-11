package org.vaccineimpact.reporting_api

import org.vaccineimpact.reporting_api.models.OrderlyReport

interface OrderlyClient
{
    fun getAllReports(): List<String>
    fun getReportsByName(name: String): List<String>
    fun getReportsByNameAndVersion(name: String, version: String): OrderlyReport
}