package org.vaccineimpact.reporting_api.db

import com.google.gson.JsonObject
interface OrderlyClient
{
    fun getAllReports(): List<String>
    fun getReportsByName(name: String): List<String>
    fun getReportsByNameAndVersion(name: String, version: String): JsonObject

    fun getArtefacts(name: String, version: String): JsonObject
    fun hasArtefact(name: String, version: String, filename: String) : Boolean

    fun getData(name: String, version: String): JsonObject
    fun getDatum(name: String, version: String, datumname: String) : String

    fun getResources(name: String, version: String): JsonObject
    fun hasResource(name: String, version: String, resourcename: String) : Boolean
}