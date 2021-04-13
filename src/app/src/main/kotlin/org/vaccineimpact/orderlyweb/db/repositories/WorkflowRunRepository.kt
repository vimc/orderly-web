package org.vaccineimpact.orderlyweb.db.repositories

import com.google.gson.Gson
import org.vaccineimpact.orderlyweb.db.*
import java.sql.Timestamp
import java.time.Instant

interface WorkflowRunRepository
{
    fun addWorkflowRun(
        name: String,
        key: String,
        user: String,
        date: Instant,
        reports: List<Map<String, Any>>,
        instances: Map<String, String>,
        gitBranch: String?,
        gitCommit: String?
    )
}

class OrderlyWebWorkflowRunRepository : WorkflowRunRepository
{
    override fun addWorkflowRun(
        name: String,
        key: String,
        user: String,
        date: Instant,
        reports: List<Map<String, Any>>,
        instances: Map<String, String>,
        gitBranch: String?,
        gitCommit: String?
    )
    {
        JooqContext().use {
            it.dsl.insertInto(Tables.ORDERLYWEB_WORKFLOW_RUN)
                .set(Tables.ORDERLYWEB_WORKFLOW_RUN.NAME, name)
                .set(Tables.ORDERLYWEB_WORKFLOW_RUN.KEY, key)
                .set(Tables.ORDERLYWEB_WORKFLOW_RUN.EMAIL, user)
                .set(Tables.ORDERLYWEB_WORKFLOW_RUN.DATE, Timestamp.from(date))
                .set(Tables.ORDERLYWEB_WORKFLOW_RUN.REPORTS, Gson().toJson(reports))
                .set(Tables.ORDERLYWEB_WORKFLOW_RUN.INSTANCES, Gson().toJson(instances))
                .set(Tables.ORDERLYWEB_WORKFLOW_RUN.GIT_BRANCH, gitBranch)
                .set(Tables.ORDERLYWEB_WORKFLOW_RUN.GIT_COMMIT, gitCommit)
                .execute()
        }
    }
}
