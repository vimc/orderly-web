package org.vaccineimpact.orderlyweb.models

import java.time.Instant

interface ReportVersion
{
    val name: String
    val displayName: String?
    val id: String
    val date: Instant
    val published: Boolean
}

data class ReportVersionWithChangelogsParams
constructor(override val name: String,
            override val displayName: String?,
            override val id: String,
            override val date: Instant,
            override val published: Boolean,
            val parameterValues: Map<String, String>,
            val changelogs: List<Changelog>): ReportVersion
