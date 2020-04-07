package org.vaccineimpact.orderlyweb.db

import org.vaccineimpact.orderlyweb.models.*

import org.vaccineimpact.orderlyweb.errors.UnknownObjectError

interface OrderlyClient
{
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

    fun getReportVersionTags(name: String, version: String): ReportVersionTags

    fun getDataInfo(name: String, version: String): List<DataInfo>

    fun getResourceFiles(name: String, version: String): List<FileInfo>

    fun getParametersForVersions(versionIds: List<String>): Map<String, Map<String, String>>

    fun getParameters(version: String): Map<String, String>

    fun getOrderlyTags(versionIds: List<String>): Map<String, List<String>>
    fun getReportTagsForVersions(versionIds: List<String>): Map<String, List<String>>
    fun getVersionTags(versionIds: List<String>): Map<String, List<String>>
}