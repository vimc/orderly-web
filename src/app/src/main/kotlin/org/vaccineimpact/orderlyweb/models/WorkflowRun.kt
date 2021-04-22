package org.vaccineimpact.orderlyweb.models

import java.time.Instant

data class WorkflowReportWithParams(
    val name: String,
    val params: Map<String, String>
)

data class WorkflowChangelog(
    val message: String,
    val type: String
)

data class WorkflowRunRequest(
    val name: String,
    val reports: List<WorkflowReportWithParams>,
    val instances: Map<String, String>? = null,
    val changelog: WorkflowChangelog? = null,
    val gitBranch: String? = null,
    val gitCommit: String? = null
)

data class WorkflowRun(
    val name: String,
    val key: String,
    val email: String,
    val date: Instant,
    val reports: List<WorkflowReportWithParams>,
    val instances: Map<String, String>,
    val gitBranch: String? = null,
    val gitCommit: String? = null
)

data class WorkflowRunSummary(
    val name: String,
    val key: String,
    val email: String,
    val date: Instant
)
