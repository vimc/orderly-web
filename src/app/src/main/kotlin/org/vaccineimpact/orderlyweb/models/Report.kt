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

data class ReportVersion(
        val basicReportVersion: BasicReportVersion,
        val customFields: Map<String, String?>,
        val parameterValues: Map<String, String>,
        val tags: List<String>) : Version by basicReportVersion
