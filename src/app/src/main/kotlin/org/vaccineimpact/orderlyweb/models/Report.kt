package org.vaccineimpact.orderlyweb.models

import java.beans.ConstructorProperties
import java.time.Instant

data class Report
@ConstructorProperties("name", "displayname", "latestVersion")
constructor(
        val name: String,
        val displayName: String?,
        val latestVersion: String
)

data class ReportWithPublishStatus
@ConstructorProperties("name", "displayname", "hasBeenPublished")
constructor(
        val name: String,
        val displayName: String?,
        val hasBeenPublished: Boolean
)

data class ReportWithDate
constructor(val name: String, val date: Instant?)

data class Parameter
constructor(val name: String, val value: String?)
