package org.vaccineimpact.reporting_api.db

import com.google.gson.JsonObject
import org.vaccineimpact.reporting_api.errors.UnknownObjectError

interface OrderlyClient
{
    fun getAllReports(): List<String>
    @Throws(UnknownObjectError::class)
    fun getReportsByName(name: String): List<String>
    @Throws(UnknownObjectError::class)
    fun getReportsByNameAndVersion(name: String, version: String): JsonObject

    @Throws(UnknownObjectError::class)
    fun getArtefacts(name: String, version: String): JsonObject
    @Throws(UnknownObjectError::class)
    fun hasArtefact(name: String, version: String, filename: String) : Boolean

    @Throws(UnknownObjectError::class)
    fun getData(name: String, version: String): JsonObject
    @Throws(UnknownObjectError::class)
    fun getDatum(name: String, version: String, datumname: String) : String

    @Throws(UnknownObjectError::class)
    fun getResources(name: String, version: String): JsonObject
    @Throws(UnknownObjectError::class)
    fun hasResource(name: String, version: String, resourcename: String) : Boolean
}