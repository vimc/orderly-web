package org.vaccineimpact.reporting_api.db

import com.google.gson.JsonObject
import org.vaccineimpact.api.models.*

import org.vaccineimpact.reporting_api.db.tables.ArtefactFormat
import org.vaccineimpact.reporting_api.errors.UnknownObjectError

interface OrderlyClient
{
    fun getAllReports(): List<Report>

    fun getAllReportVersions(): List<ReportVersion>

    @Throws(UnknownObjectError::class)
    fun getReportsByName(name: String): List<String>

    @Throws(UnknownObjectError::class)
    fun getReportByNameAndVersion(name: String, version: String): JsonObject

    @Throws(UnknownObjectError::class)
    fun getDetailsByNameAndVersion(name: String, version: String): ReportVersionDetails

    @Throws(UnknownObjectError::class)
    fun getChangelogByNameAndVersion(name: String, version: String): List<Changelog>

    @Throws(UnknownObjectError::class)
    fun getArtefactHashes(name: String, version: String): Map<String, String>

    @Throws(UnknownObjectError::class)
    fun getArtefacts(report: String, version: String): List<Artefact>

    @Throws(UnknownObjectError::class)
    fun getArtefactHash(name: String, version: String, filename: String): String

    @Throws(UnknownObjectError::class)
    fun getData(name: String, version: String): JsonObject

    @Throws(UnknownObjectError::class)
    fun getDatum(name: String, version: String, datumname: String): String

    @Throws(UnknownObjectError::class)
    fun getResourceHashes(name: String, version: String): Map<String, String>

    @Throws(UnknownObjectError::class)
    fun getResourceHash(name: String, version: String, resourcename: String): String

    @Throws(UnknownObjectError::class)
    fun getLatestChangelogByName(name: String): List<Changelog>
}