package org.vaccineimpact.orderlyweb.db.repositories

import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.errors.BadRequest
import org.vaccineimpact.orderlyweb.db.Tables.ORDERLYWEB_WORKFLOW_RUN_REPORTS
import org.vaccineimpact.orderlyweb.db.Tables.ORDERLYWEB_WORKFLOW_RUN
import org.vaccineimpact.orderlyweb.errors.UnknownObjectError
import org.vaccineimpact.orderlyweb.jsonToStringMap
import org.vaccineimpact.orderlyweb.models.ReportRunLog

interface  WorkflowRunReportRepository: ReportRunLogRepository
{
    @Throws(BadRequest::class)
    fun checkReportIsInWorkflow(reportKey: String, workflowKey: String)
}


class OrderlyWebWorkflowRunReportRepository: WorkflowRunReportRepository
{
    override fun checkReportIsInWorkflow(reportKey: String, workflowKey: String)
    {
        JooqContext().use {
            val result = it.dsl.selectCount()
                    .from(ORDERLYWEB_WORKFLOW_RUN_REPORTS)
                    .where(ORDERLYWEB_WORKFLOW_RUN_REPORTS.KEY.eq(reportKey)
                        .and(ORDERLYWEB_WORKFLOW_RUN_REPORTS.WORKFLOW_KEY.eq(workflowKey)))
            if (result.count() != 1)
            {
                throw BadRequest("Report with key $reportKey does not belong to workflow with key $workflowKey")
            }
        }
    }

    override fun updateReportRun(key: String, status: String, version: String?, logs: List<String>?)
    {
        val logsString = logs?.joinToString(separator = "\n") //TODO: DO this in caller?

        val reportVersion = if (status == OrderlyWebReportRunRepository.SUCCESS_STATUS)
        {
            version
        }
        else
        {
            null
        }
        JooqContext().use {
            it.dsl.update(ORDERLYWEB_WORKFLOW_RUN_REPORTS)
                    .set(ORDERLYWEB_WORKFLOW_RUN_REPORTS.STATUS, status)
                    //TODO: REPORT VERSION NEEDS TO BE ADDED TO THE TABLE
                    //.set(ORDERLYWEB_WORKFLOW_RUN_REPORTS.REPORT_VERSION, reportVersion)
                    .set(ORDERLYWEB_WORKFLOW_RUN_REPORTS.LOGS, logsString)
                    .where(ORDERLYWEB_WORKFLOW_RUN_REPORTS.KEY.eq(key))
                    .execute()
        }
    }

    override fun getReportRun(key: String): ReportRunLog
    {
        //TODO: get report version as well
        JooqContext().use {
            val result = it.dsl.select(
                    ORDERLYWEB_WORKFLOW_RUN_REPORTS.REPORT,
                    ORDERLYWEB_WORKFLOW_RUN_REPORTS.PARAMS,
                    ORDERLYWEB_WORKFLOW_RUN_REPORTS.STATUS,
                    ORDERLYWEB_WORKFLOW_RUN_REPORTS.LOGS,
                    ORDERLYWEB_WORKFLOW_RUN_REPORTS.DATE, // NB This will always be null until mrc-2898 is done
                    ORDERLYWEB_WORKFLOW_RUN.EMAIL,
                    ORDERLYWEB_WORKFLOW_RUN.INSTANCES,
                    ORDERLYWEB_WORKFLOW_RUN.GIT_BRANCH,
                    ORDERLYWEB_WORKFLOW_RUN.GIT_COMMIT,)
                    .from(ORDERLYWEB_WORKFLOW_RUN_REPORTS)
                    .join(ORDERLYWEB_WORKFLOW_RUN)
                    .on(ORDERLYWEB_WORKFLOW_RUN.KEY.eq(ORDERLYWEB_WORKFLOW_RUN_REPORTS.WORKFLOW_KEY))
                    .where(ORDERLYWEB_WORKFLOW_RUN_REPORTS.KEY.eq(key))
                    .singleOrNull()
                    ?: throw UnknownObjectError("key", "getReportRun")

            return ReportRunLog(result[ORDERLYWEB_WORKFLOW_RUN.EMAIL],
                    result[ORDERLYWEB_WORKFLOW_RUN_REPORTS.DATE]?.toInstant(),
                    result[ORDERLYWEB_WORKFLOW_RUN_REPORTS.REPORT],
                    jsonToStringMap(result[ORDERLYWEB_WORKFLOW_RUN.INSTANCES]),
                    jsonToStringMap(result[ORDERLYWEB_WORKFLOW_RUN_REPORTS.PARAMS]),
                    result[ORDERLYWEB_WORKFLOW_RUN.GIT_BRANCH],
                    result[ORDERLYWEB_WORKFLOW_RUN.GIT_COMMIT],
                    result[ORDERLYWEB_WORKFLOW_RUN_REPORTS.STATUS],
                    result[ORDERLYWEB_WORKFLOW_RUN_REPORTS.LOGS],
                    null //TODO: get report version from workflow run report table
            )
        }
    }
}
