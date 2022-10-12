package org.vaccineimpact.orderlyweb.models

data class ReportVersionTags(
        val versionTags: List<String>,
        val reportTags: List<String>,
        val orderlyTags: List<String>
)
