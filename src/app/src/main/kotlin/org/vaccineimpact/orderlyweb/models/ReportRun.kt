package org.vaccineimpact.orderlyweb.models

import java.time.Instant

data class ReportRun(val name: String, val key: String, val path: String)

data class ReportStatus(
    val key: String,
    val status: String,
    val name: String,
    val version: String,
    val output: List<String>? = null,
    val queue: List<Any>? = null
)

data class ReportRunLog(
    val email: String,
    val date: Instant,
    val report: String,
    val instances: Map<String, String>?,
    val params: Map<String, String>?,
    val gitBranch: String?,
    val gitCommit: String?,
    val status: String?,
    val logs: String?,
    val reportVersion: String?
)
