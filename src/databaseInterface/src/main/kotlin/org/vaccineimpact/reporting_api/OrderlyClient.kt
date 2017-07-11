package org.vaccineimpact.reporting_api

import org.json.JSONObject

interface OrderlyClient
{
    fun getAllReports(): List<String>
    fun getReportsByName(name: String): List<String>
    fun getReportsByNameAndVersion(name: String, version: String): OrderlyReport
    fun getArtefacts(name: String, version: String) : JSONObject

}