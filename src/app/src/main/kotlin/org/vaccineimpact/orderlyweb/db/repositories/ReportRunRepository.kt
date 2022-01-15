package org.vaccineimpact.orderlyweb.db.repositories

import com.google.gson.Gson
import org.vaccineimpact.orderlyweb.db.*
import org.vaccineimpact.orderlyweb.db.Tables.ORDERLYWEB_REPORT_RUN
import org.vaccineimpact.orderlyweb.errors.UnknownObjectError
import org.vaccineimpact.orderlyweb.models.ReportRunWithDate
import java.sql.Timestamp
import java.time.Instant
import org.vaccineimpact.orderlyweb.jsonToStringMap
import org.vaccineimpact.orderlyweb.models.ReportRunLog

interface ReportRunRepository
{
    fun addReportRun(
        key: String,
        user: String,
        date: Instant,
        report: String,
        instances: Map<String, String>,
        params: Map<String, String>,
        gitBranch: String?,
        gitCommit: String?,
        workflowRunKey: String? = null
    )

    @Throws(UnknownObjectError::class)
    fun getReportRun(key: String): ReportRunLog

    fun getAllReportRunsForUser(user: String): List<ReportRunWithDate>

    fun updateReportRun(
        key: String,
        status: String,
        version: String?,
        logs: List<String>?
    )
}

class OrderlyWebReportRunRepository : ReportRunRepository
{
    companion object {
        const val SUCCESS_STATUS = "success"
    }

    override fun addReportRun(
        key: String,
        user: String,
        date: Instant,
        report: String,
        instances: Map<String, String>,
        params: Map<String, String>,
        gitBranch: String?,
        gitCommit: String?,
        workflowRunKey: String?
    )
    {
        JooqContext().use {
            it.dsl.insertInto(Tables.ORDERLYWEB_REPORT_RUN)
                .set(ORDERLYWEB_REPORT_RUN.KEY, key)
                .set(ORDERLYWEB_REPORT_RUN.EMAIL, user)
                .set(ORDERLYWEB_REPORT_RUN.DATE, Timestamp.from(date))
                .set(ORDERLYWEB_REPORT_RUN.REPORT, report)
                .set(ORDERLYWEB_REPORT_RUN.INSTANCES, Gson().toJson(instances))
                .set(ORDERLYWEB_REPORT_RUN.PARAMS, Gson().toJson(params))
                .set(ORDERLYWEB_REPORT_RUN.GIT_BRANCH, gitBranch)
                .set(ORDERLYWEB_REPORT_RUN.GIT_COMMIT, gitCommit)
                .set(ORDERLYWEB_REPORT_RUN.WORKFLOW_RUN_KEY, workflowRunKey)
                .execute()
        }
    }

    override fun getReportRun(key: String): ReportRunLog
    {
        JooqContext().use {
            val result = it.dsl.select(
                    ORDERLYWEB_REPORT_RUN.EMAIL,
                    ORDERLYWEB_REPORT_RUN.DATE,
                    ORDERLYWEB_REPORT_RUN.REPORT,
                    ORDERLYWEB_REPORT_RUN.INSTANCES,
                    ORDERLYWEB_REPORT_RUN.PARAMS,
                    ORDERLYWEB_REPORT_RUN.GIT_BRANCH,
                    ORDERLYWEB_REPORT_RUN.GIT_COMMIT,
                    ORDERLYWEB_REPORT_RUN.STATUS,
                    ORDERLYWEB_REPORT_RUN.LOGS,
                    ORDERLYWEB_REPORT_RUN.REPORT_VERSION)
                    .from(ORDERLYWEB_REPORT_RUN)
                    .where(ORDERLYWEB_REPORT_RUN.KEY.eq(key))
                    .singleOrNull()
                    ?: throw UnknownObjectError("key", "getReportRun")

            return ReportRunLog(result[ORDERLYWEB_REPORT_RUN.EMAIL],
                    result[ORDERLYWEB_REPORT_RUN.DATE].toInstant(),
                    result[ORDERLYWEB_REPORT_RUN.REPORT],
                    jsonToStringMap(result[ORDERLYWEB_REPORT_RUN.INSTANCES]),
                    jsonToStringMap(result[ORDERLYWEB_REPORT_RUN.PARAMS]),
                    result[ORDERLYWEB_REPORT_RUN.GIT_BRANCH],
                    result[ORDERLYWEB_REPORT_RUN.GIT_COMMIT],
                    result[ORDERLYWEB_REPORT_RUN.STATUS],
                    result[ORDERLYWEB_REPORT_RUN.LOGS],
                    result[ORDERLYWEB_REPORT_RUN.REPORT_VERSION])
        }
    }

    override fun getAllReportRunsForUser(user: String): List<ReportRunWithDate>
    {
        JooqContext().use {
            val result = it.dsl.select(
                    ORDERLYWEB_REPORT_RUN.REPORT.`as`("name"),
                    ORDERLYWEB_REPORT_RUN.KEY,
                    ORDERLYWEB_REPORT_RUN.DATE
            )
                    .from(ORDERLYWEB_REPORT_RUN)
                    .where(ORDERLYWEB_REPORT_RUN.EMAIL.eq(user).and(ORDERLYWEB_REPORT_RUN.WORKFLOW_RUN_KEY.isNull))

            return result.fetchInto(ReportRunWithDate::class.java)
        }
    }

    override fun updateReportRun(
        key: String,
        status: String,
        version: String?,
        logs: List<String>?
    )
    {
        val logsString = logs?.joinToString(separator = "\n")
        val reportVersion = if (status == SUCCESS_STATUS)
        {
            version
        }
        else
        {
            null
        }
        JooqContext().use {
            it.dsl.update(ORDERLYWEB_REPORT_RUN)
                    .set(ORDERLYWEB_REPORT_RUN.STATUS, status)
                    .set(ORDERLYWEB_REPORT_RUN.REPORT_VERSION, reportVersion)
                    .set(ORDERLYWEB_REPORT_RUN.LOGS, logsString)
                    .where(ORDERLYWEB_REPORT_RUN.KEY.eq(key))
                    .execute()
        }
    }
}
