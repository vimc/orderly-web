package org.vaccineimpact.orderlyweb.db.repositories

import com.google.gson.Gson
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.Tables
import org.vaccineimpact.orderlyweb.models.WorkflowRun
import java.sql.Timestamp
import java.time.Instant

interface WorkflowRunRepository
{
    fun addWorkflowRun(
        workflowRun: WorkflowRun,
        key: String,
        user: String,
        date: Instant
    )
}

class OrderlyWebWorkflowRunRepository : WorkflowRunRepository
{
    override fun addWorkflowRun(workflowRun: WorkflowRun, key: String, user: String, date: Instant)
    {
        JooqContext().use {
            it.dsl.insertInto(Tables.ORDERLYWEB_WORKFLOW_RUN)
                .set(Tables.ORDERLYWEB_WORKFLOW_RUN.NAME, workflowRun.name)
                .set(Tables.ORDERLYWEB_WORKFLOW_RUN.KEY, key)
                .set(Tables.ORDERLYWEB_WORKFLOW_RUN.EMAIL, user)
                .set(Tables.ORDERLYWEB_WORKFLOW_RUN.DATE, Timestamp.from(date))
                .set(Tables.ORDERLYWEB_WORKFLOW_RUN.REPORTS, Gson().toJson(workflowRun.reports))
                .set(Tables.ORDERLYWEB_WORKFLOW_RUN.INSTANCES, Gson().toJson(workflowRun.instances))
                .set(Tables.ORDERLYWEB_WORKFLOW_RUN.GIT_BRANCH, workflowRun.gitBranch)
                .set(Tables.ORDERLYWEB_WORKFLOW_RUN.GIT_COMMIT, workflowRun.gitCommit)
                .execute()
        }
    }
}
