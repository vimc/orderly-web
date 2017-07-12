package org.vaccineimpact.reporting_api

interface OrderlyClient
{
    fun getAllReports(): List<String>
    fun getReportsByName(name: String): List<String>
    fun getReportsByNameAndVersion(name: String, version: String): OrderlyReport

    fun hasArtefact(name: String, version: String, filename: String) : Boolean
    fun getArtefacts(name: String, version: String): Map<String, String>

    fun getData(name: String, version: String): Map<String, String>
    fun hasData(name: String, version: String, dataname: String) : Boolean
}