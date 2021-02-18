package org.vaccineimpact.orderlyweb.models

import com.google.gson.Gson
import java.time.Instant
import java.util.*

data class ReportRun(val name: String, val key: String, val path: String)

data class ReportRunLog
constructor(
        val email: String,
        val date: Instant,
        val report: String,
        val instances: Any?,
        val params: Any?,
        val gitBranch: String?,
        val gitCommit: String?,
        val status: String?,
        val logs: String?,
        val reportVersion: String?
)