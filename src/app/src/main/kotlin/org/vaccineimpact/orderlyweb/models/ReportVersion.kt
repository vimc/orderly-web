package org.vaccineimpact.orderlyweb.models

import java.beans.ConstructorProperties
import java.time.Instant

data class BasicReportVersion
@ConstructorProperties("name", "displayname", "id", "published", "date", "latestVersion", "description")
constructor(val name: String,
            val displayName: String?,
            val id: String,
            val published: Boolean,
            val date: Instant,
            val latestVersion: String,
            val description: String?)

data class ReportVersion(@Transient val basicReportVersion: BasicReportVersion,
                         val customFields: Map<String, String?>,
                         val parameterValues: Map<String, String>,
                         val tags: List<String>)
{
    val date: Instant = basicReportVersion.date
    val description: String? = basicReportVersion.description
    val displayName: String? = basicReportVersion.displayName
    val id: String = basicReportVersion.id
    val name: String = basicReportVersion.name
    val published: Boolean = basicReportVersion.published
    val latestVersion: String = basicReportVersion.latestVersion
}

data class ReportVersionWithChangelog
constructor(@Transient val basicReportVersion: BasicReportVersion,
            val parameterValues: Map<String, String>,
            val changelogs: List<Changelog>)
{
    val date: Instant = basicReportVersion.date
    val description: String? = basicReportVersion.description
    val displayName: String? = basicReportVersion.displayName
    val id: String = basicReportVersion.id
    val name: String = basicReportVersion.name
}
