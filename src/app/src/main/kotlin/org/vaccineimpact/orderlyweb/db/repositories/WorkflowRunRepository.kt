package org.vaccineimpact.orderlyweb.db.repositories

import com.google.gson.Gson
import org.jooq.impl.DSL.lower
import org.jooq.impl.DSL.noCondition
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.Tables
import org.vaccineimpact.orderlyweb.models.WorkflowRun
import org.vaccineimpact.orderlyweb.models.WorkflowRunSummary
import java.sql.Timestamp

interface WorkflowRunRepository
{
    fun addWorkflowRun(workflowRun: WorkflowRun)
    fun getWorkflowRunSummaries(email: String? = null, namePrefix: String? = null): List<WorkflowRunSummary>
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

    override fun getWorkflowRunSummaries(email: String?, namePrefix: String?): List<WorkflowRunSummary>
    {
        JooqContext().use {
            val result = it.dsl.select(
                Tables.ORDERLYWEB_WORKFLOW_RUN.NAME,
                Tables.ORDERLYWEB_WORKFLOW_RUN.KEY,
                Tables.ORDERLYWEB_WORKFLOW_RUN.EMAIL,
                Tables.ORDERLYWEB_WORKFLOW_RUN.DATE
            )
                .from(Tables.ORDERLYWEB_WORKFLOW_RUN)
                .where(
                    if (email != null)
                    {
                        Tables.ORDERLYWEB_WORKFLOW_RUN.EMAIL.eq(email)
                    }
                    else
                    {
                        noCondition()
                    }
                )
                .and(
                    if (namePrefix != null)
                    {
                        lower(Tables.ORDERLYWEB_WORKFLOW_RUN.NAME).startsWith(namePrefix.toLowerCase())
                    }
                    else
                    {
                        noCondition()
                    }
                )

            return result.fetchInto(WorkflowRunSummary::class.java)
        }
    }
}
