package org.vaccineimpact.orderlyweb.models

import java.time.Instant

data class WorkflowReportWithParams(
    val name: String,
    val params: Map<String, String>
)

data class WorkflowRun(
    val name: String,
    val key: String,
    val user: String,
    val date: Instant,
    val reports: List<WorkflowReportWithParams>,
    val instances: Map<String, String>,
    val gitBranch: String? = null,
    val gitCommit: String? = null
)
