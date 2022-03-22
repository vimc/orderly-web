package org.vaccineimpact.orderlyweb.db.repositories

import com.google.gson.Gson
import org.jooq.impl.DSL.*
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.Tables.ORDERLYWEB_WORKFLOW_RUN
import org.vaccineimpact.orderlyweb.db.Tables.ORDERLYWEB_WORKFLOW_RUN_REPORTS
import org.vaccineimpact.orderlyweb.errors.UnknownObjectError
import org.vaccineimpact.orderlyweb.jsonToStringMap
import org.vaccineimpact.orderlyweb.models.WorkflowRun
import org.vaccineimpact.orderlyweb.models.WorkflowRunReport
import org.vaccineimpact.orderlyweb.models.WorkflowRunSummary
import java.sql.Timestamp

interface WorkflowRunRepository
{
    fun addWorkflowRun(workflowRun: WorkflowRun)

    @Throws(UnknownObjectError::class)
    fun getWorkflowRunDetails(key: String): WorkflowRun
    fun getWorkflowRunSummaries(email: String? = null, namePrefix: String? = null): List<WorkflowRunSummary>

    @Throws(UnknownObjectError::class)
    fun updateWorkflowRun(key: String, status: String)
}

class OrderlyWebWorkflowRunRepository : WorkflowRunRepository
{
    override fun addWorkflowRun(workflowRun: WorkflowRun)
    {
        JooqContext().use {

            it.dsl.transaction { config ->
                val dsl = using(config)

                dsl.insertInto(ORDERLYWEB_WORKFLOW_RUN)
                        .set(ORDERLYWEB_WORKFLOW_RUN.NAME, workflowRun.name)
                        .set(ORDERLYWEB_WORKFLOW_RUN.KEY, workflowRun.key)
                        .set(ORDERLYWEB_WORKFLOW_RUN.EMAIL, workflowRun.email)
                        .set(ORDERLYWEB_WORKFLOW_RUN.DATE, Timestamp.from(workflowRun.date))
                        .set(ORDERLYWEB_WORKFLOW_RUN.INSTANCES, Gson().toJson(workflowRun.instances))
                        .set(ORDERLYWEB_WORKFLOW_RUN.GIT_BRANCH, workflowRun.gitBranch)
                        .set(ORDERLYWEB_WORKFLOW_RUN.GIT_COMMIT, workflowRun.gitCommit)
                        .execute()

                workflowRun.reports.forEach { report ->
                    dsl.insertInto(ORDERLYWEB_WORKFLOW_RUN_REPORTS)
                            .set(ORDERLYWEB_WORKFLOW_RUN_REPORTS.WORKFLOW_KEY, report.workflowKey)
                            .set(ORDERLYWEB_WORKFLOW_RUN_REPORTS.KEY, report.key)
                            .set(ORDERLYWEB_WORKFLOW_RUN_REPORTS.REPORT, report.report)
                            .set(ORDERLYWEB_WORKFLOW_RUN_REPORTS.PARAMS, Gson().toJson(report.params))
                            .execute()
                }
            }
        }
    }

    override fun getWorkflowRunDetails(key: String): WorkflowRun
    {
        JooqContext().use {
            val result = it.dsl.select(
                    ORDERLYWEB_WORKFLOW_RUN.NAME,
                    ORDERLYWEB_WORKFLOW_RUN.KEY,
                    ORDERLYWEB_WORKFLOW_RUN.EMAIL,
                    ORDERLYWEB_WORKFLOW_RUN.DATE,
                    ORDERLYWEB_WORKFLOW_RUN.INSTANCES,
                    ORDERLYWEB_WORKFLOW_RUN.GIT_BRANCH,
                    ORDERLYWEB_WORKFLOW_RUN.GIT_COMMIT
            )
                    .from(ORDERLYWEB_WORKFLOW_RUN)
                    .where(ORDERLYWEB_WORKFLOW_RUN.KEY.eq(key))
                    .singleOrNull()
                    ?: throw UnknownObjectError(key, "workflow")

            val resultForReports = it.dsl.select(
                    ORDERLYWEB_WORKFLOW_RUN_REPORTS.WORKFLOW_KEY,
                    ORDERLYWEB_WORKFLOW_RUN_REPORTS.KEY,
                    ORDERLYWEB_WORKFLOW_RUN_REPORTS.REPORT,
                    ORDERLYWEB_WORKFLOW_RUN_REPORTS.PARAMS
            )
                    .from(ORDERLYWEB_WORKFLOW_RUN)
                    .join(ORDERLYWEB_WORKFLOW_RUN_REPORTS)
                    .on(ORDERLYWEB_WORKFLOW_RUN.KEY.eq(ORDERLYWEB_WORKFLOW_RUN_REPORTS.WORKFLOW_KEY))
                    .where(ORDERLYWEB_WORKFLOW_RUN.KEY.eq(key))
                    .fetch()

            return WorkflowRun(
                    result[ORDERLYWEB_WORKFLOW_RUN.NAME],
                    result[ORDERLYWEB_WORKFLOW_RUN.KEY],
                    result[ORDERLYWEB_WORKFLOW_RUN.EMAIL],
                    result[ORDERLYWEB_WORKFLOW_RUN.DATE].toInstant(),
                    resultForReports.map { report ->
                        WorkflowRunReport(
                                report[ORDERLYWEB_WORKFLOW_RUN_REPORTS.WORKFLOW_KEY],
                                report[ORDERLYWEB_WORKFLOW_RUN_REPORTS.KEY],
                                report[ORDERLYWEB_WORKFLOW_RUN_REPORTS.REPORT],
                                jsonToStringMap(report[ORDERLYWEB_WORKFLOW_RUN_REPORTS.PARAMS])
                        )
                    },
                    jsonToStringMap(result[ORDERLYWEB_WORKFLOW_RUN.INSTANCES]),
                    result[ORDERLYWEB_WORKFLOW_RUN.GIT_BRANCH],
                    result[ORDERLYWEB_WORKFLOW_RUN.GIT_COMMIT]
            )
        }
    }

    override fun getWorkflowRunSummaries(email: String?, namePrefix: String?): List<WorkflowRunSummary>
    {
        JooqContext().use {
            val result = it.dsl.select(
                    ORDERLYWEB_WORKFLOW_RUN.NAME,
                    ORDERLYWEB_WORKFLOW_RUN.KEY,
                    ORDERLYWEB_WORKFLOW_RUN.EMAIL,
                    ORDERLYWEB_WORKFLOW_RUN.DATE
            )
                    .from(ORDERLYWEB_WORKFLOW_RUN)
                    .where(
                            if (email != null)
                            {
                                ORDERLYWEB_WORKFLOW_RUN.EMAIL.eq(email)
                            }
                            else
                            {
                                noCondition()
                            }
                    )
                    .and(
                            if (namePrefix != null)
                            {
                                lower(ORDERLYWEB_WORKFLOW_RUN.NAME).startsWith(namePrefix.toLowerCase())
                            }
                            else
                            {
                                noCondition()
                            }
                    )
                    .orderBy(ORDERLYWEB_WORKFLOW_RUN.DATE.desc())

            return result.fetchInto(WorkflowRunSummary::class.java)
        }
    }

    override fun updateWorkflowRun(key: String, status: String)
    {
        JooqContext().use {
            val result = it.dsl.update(ORDERLYWEB_WORKFLOW_RUN)
                    .set(ORDERLYWEB_WORKFLOW_RUN.STATUS, status)
                    .where(ORDERLYWEB_WORKFLOW_RUN.KEY.eq(key))
                    .execute()
            if (result == 0)
            {
                throw UnknownObjectError(key, "workflow")
            }
        }
    }
}
