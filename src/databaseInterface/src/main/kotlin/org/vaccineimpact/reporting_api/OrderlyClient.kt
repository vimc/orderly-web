package org.vaccineimpact.reporting_api

interface OrderlyClient
{
    fun getAllReports(): List<String>
    fun getReportsByName(name: String): List<String>
    fun getReportsByNameAndVersion(name: String, version: String): OrderlyReport
    fun getArtefacts(name: String, version: String) : ArrayList<Artefact>
    fun hasArtefact(name: String, version: String, filename: String) : Boolean

}