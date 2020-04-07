package org.vaccineimpact.orderlyweb.models

import java.beans.ConstructorProperties
import java.time.Instant

data class Report
@ConstructorProperties("name", "displayname", "latestVersion")
constructor(val name: String,
            val displayName: String?,
            val latestVersion: String)

interface Version
{
    val name: String
    val displayName: String?
    val id: String
    val published: Boolean
    val date: Instant
    val latestVersion: String
    val description: String?
}

data class BasicReportVersion
@ConstructorProperties("name", "displayname", "id", "published", "date", "latestVersion", "description")
constructor(override val name: String,
            override val displayName: String?,
            override val id: String,
            override val published: Boolean,
            override val date: Instant,
            override val latestVersion: String,
            override val description: String?) : Version

data class ReportVersion(@Transient val basicReportVersion: BasicReportVersion,
        val customFields: Map<String, String?>,
        val parameterValues: Map<String, String>,
        val tags: List<String>) : Version by basicReportVersion
{
    // we have to declare these overrides so that this gets serialised as a flat object
    override val date: Instant = basicReportVersion.date
    override val description: String? = basicReportVersion.description
    override val displayName: String? = basicReportVersion.displayName
    override val id: String = basicReportVersion.id
    override val name: String = basicReportVersion.name
    override val latestVersion: String = basicReportVersion.latestVersion
    override val published: Boolean = basicReportVersion.published
}
