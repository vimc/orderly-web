package org.vaccineimpact.reporting_api.db

import com.google.gson.JsonObject

import org.vaccineimpact.api.models.Changelog
import org.vaccineimpact.api.models.Report
import org.vaccineimpact.api.models.ReportVersion
import org.vaccineimpact.reporting_api.errors.UnknownObjectError

interface OrderlyClient
{
    fun getAllReports(): List<Report>

    fun getAllReportVersions(): List<ReportVersion>

    @Throws(UnknownObjectError::class)
    fun getReportsByName(name: String): List<String>

    @Throws(UnknownObjectError::class)
    fun getReportsByNameAndVersion(name: String, version: String): JsonObject

    @Throws(UnknownObjectError::class)
    fun getChangelogByNameAndVersion(name: String, version: String): List<Changelog>

    @Throws(UnknownObjectError::class)
    fun getArtefacts(name: String, version: String): JsonObject

    @Throws(UnknownObjectError::class)
    fun getArtefact(name: String, version: String, filename: String): String

    @Throws(UnknownObjectError::class)
    fun getData(name: String, version: String): JsonObject

    @Throws(UnknownObjectError::class)
    fun getDatum(name: String, version: String, datumname: String): String

    @Throws(UnknownObjectError::class)
    fun getResources(name: String, version: String): JsonObject

    @Throws(UnknownObjectError::class)
    fun getResource(name: String, version: String, resourcename: String): String

    @Throws(UnknownObjectError::class)
    fun getLatestChangelogByName(name: String): List<Changelog>
}