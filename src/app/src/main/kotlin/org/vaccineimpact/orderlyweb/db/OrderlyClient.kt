package org.vaccineimpact.orderlyweb.db

import org.vaccineimpact.orderlyweb.models.*

import org.vaccineimpact.orderlyweb.errors.UnknownObjectError

interface OrderlyClient
{
    fun getAllReports(): List<Report>

    fun getAllReportVersions(): List<ReportVersion>

    fun getGlobalPinnedReports(): List<ReportVersion>

    @Throws(UnknownObjectError::class)
    fun getReportsByName(name: String): List<String>

    @Throws(UnknownObjectError::class)
    fun checkVersionExistsForReport(name: String, version: String)

    @Throws(UnknownObjectError::class)
    fun getDetailsByNameAndVersion(name: String, version: String): ReportVersionDetails

    @Throws(UnknownObjectError::class)
    fun getChangelogByNameAndVersion(name: String, version: String): List<Changelog>

    @Throws(UnknownObjectError::class)
    fun getData(name: String, version: String): Map<String, String>

    @Throws(UnknownObjectError::class)
    fun getDatum(name: String, version: String, datumname: String): String

    @Throws(UnknownObjectError::class)
    fun getResourceHashes(name: String, version: String): Map<String, String>

    @Throws(UnknownObjectError::class)
    fun getResourceHash(name: String, version: String, resourcename: String): String

    @Throws(UnknownObjectError::class)
    fun getLatestChangelogByName(name: String): List<Changelog>

    fun getReadme(name: String, version: String): Map<String, String>

    fun togglePublishStatus(name: String, version: String): Boolean

    fun getReportVersionTags(name: String, version: String): ReportVersionTags

}