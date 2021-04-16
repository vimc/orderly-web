package org.vaccineimpact.orderlyweb.db.repositories

import com.google.gson.Gson
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.Tables
import org.vaccineimpact.orderlyweb.models.WorkflowRun
import java.sql.Timestamp

interface WorkflowRunRepository
{
    fun addWorkflowRun(workflowRun: WorkflowRun)
}

class OrderlyWebWorkflowRunRepository : WorkflowRunRepository
{
    override fun addWorkflowRun(workflowRun: WorkflowRun)
    {
        JooqContext().use {
            it.dsl.insertInto(Tables.ORDERLYWEB_WORKFLOW_RUN)
                .set(Tables.ORDERLYWEB_WORKFLOW_RUN.NAME, workflowRun.name)
                .set(Tables.ORDERLYWEB_WORKFLOW_RUN.KEY, workflowRun.key)
                .set(Tables.ORDERLYWEB_WORKFLOW_RUN.EMAIL, workflowRun.email)
                .set(Tables.ORDERLYWEB_WORKFLOW_RUN.DATE, Timestamp.from(workflowRun.date))
                .set(Tables.ORDERLYWEB_WORKFLOW_RUN.REPORTS, Gson().toJson(workflowRun.reports))
                .set(Tables.ORDERLYWEB_WORKFLOW_RUN.INSTANCES, Gson().toJson(workflowRun.instances))
                .set(Tables.ORDERLYWEB_WORKFLOW_RUN.GIT_BRANCH, workflowRun.gitBranch)
                .set(Tables.ORDERLYWEB_WORKFLOW_RUN.GIT_COMMIT, workflowRun.gitCommit)
                .execute()
        }
    }
}
