package org.vaccineimpact.orderlyweb.models

import java.time.Instant

data class ReportDraft
constructor(val name: String,
            val displayName: String?,
            val id: String,
            val date: Instant,
            val parameterValues: Map<String, String>,
            val changelogs: List<Changelog>)
