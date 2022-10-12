package org.vaccineimpact.orderlyweb.models

import java.beans.ConstructorProperties
import java.time.Instant

data class ReportVersionWithDescLatest
@ConstructorProperties("name", "displayname", "id", "published", "date", "latestVersion", "description")
constructor(
        override val name: String,
        override val displayName: String?,
        override val id: String,
        override val published: Boolean,
        override val date: Instant,
        val latestVersion: String,
        val description: String?
) : ReportVersion
