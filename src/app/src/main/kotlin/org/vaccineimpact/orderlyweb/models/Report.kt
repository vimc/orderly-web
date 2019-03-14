package org.vaccineimpact.orderlyweb.models

import java.beans.ConstructorProperties
import java.time.Instant

data class Report
@ConstructorProperties("name", "displayname", "latestVersion")
constructor(val name: String,
            val displayName: String?,
            val latestVersion: String)

data class ReportVersion
@ConstructorProperties("name", "displayname", "id", "latestVersion", "published", "date", "author",
        "requester")
constructor(val name: String,
            val displayName: String?,
            val id: String,
            val latestVersion: String,
            val published: Boolean,
            val date: Instant,
            val author: String,
            val requester: String)