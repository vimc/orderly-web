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
        val reports: List<WorkflowRunReport>,
        val instances: Map<String, String>,
        val gitBranch: String? = null,
        val gitCommit: String? = null
)

data class WorkflowRunStatus(
        val status: String,
        val reports: List<WorkflowRunReportStatus>
)
{
    data class WorkflowRunReportStatus(
            val name: String,
            val key: String,
            val status: String,
            val version: String? = null
    )
}

data class WorkflowRunSummary(
        val name: String,
        val key: String,
        val email: String,
        val date: Instant
)

data class WorkflowRunReport(
        val workflowKey: String,
        val key: String,
        val report: String,
        val params: Map<String, String>
)

data class WorkflowReportWithDependencies(
        val name: String,
        val instance: String?,
        val params: Map<String, String>?,
        val paramList: List<Parameter>?,
        val defaultParamList: List<Parameter>?,
        val dependsOn: List<String>?
)

data class WorkflowSummary(
        val reports: List<WorkflowReportWithDependencies>,
        val ref: String,
        val missingDependencies: Map<String, List<String>>
)
