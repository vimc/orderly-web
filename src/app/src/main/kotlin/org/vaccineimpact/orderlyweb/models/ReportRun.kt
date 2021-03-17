package org.vaccineimpact.orderlyweb.models

import java.time.Instant

data class ReportRun(val name: String, val key: String, val path: String)

data class ReportRunLog
(
        val email: String,
        val date: Instant,
        val report: String,
        val instances: String,
        val params: String,
        val gitBranch: String,
        val gitCommit: String,
        val status: String,
        val logs: String,
        val reportVersion: String
)

data class Running(val date: Instant,
    val name: String,
    val key: String)
