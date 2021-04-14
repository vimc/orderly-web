package org.vaccineimpact.orderlyweb.models

data class WorkflowReportWithParams(
    val name: String,
    val params: Map<String, String>
)

data class WorkflowRun(
    val name: String,
    val reports: List<WorkflowReportWithParams>,
    val instances: Map<String, String>,
    val gitBranch: String? = null,
    val gitCommit: String? = null
)
