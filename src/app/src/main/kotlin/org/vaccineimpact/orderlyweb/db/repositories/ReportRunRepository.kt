package org.vaccineimpact.orderlyweb.db.repositories

import com.google.gson.Gson
import org.vaccineimpact.orderlyweb.db.*
import org.vaccineimpact.orderlyweb.db.Tables.ORDERLYWEB_REPORT_RUN
import org.vaccineimpact.orderlyweb.errors.UnknownObjectError
import org.vaccineimpact.orderlyweb.models.ReportRunLog
import org.vaccineimpact.orderlyweb.models.Running
import java.sql.Timestamp
import java.time.Instant

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
        gitCommit: String?
    )
    fun getAllRunningReports(user: String): List<Running>
    fun getReportRun(key: String): List<ReportRunLog>
}

class OrderlyWebReportRunRepository : ReportRunRepository
{
    override fun addReportRun(
        key: String,
        user: String,
        date: Instant,
        report: String,
        instances: Map<String, String>,
        params: Map<String, String>,
        gitBranch: String?,
        gitCommit: String?
    )
    {
        JooqContext().use {
            it.dsl.insertInto(Tables.ORDERLYWEB_REPORT_RUN)
                .set(Tables.ORDERLYWEB_REPORT_RUN.KEY, key)
                .set(Tables.ORDERLYWEB_REPORT_RUN.EMAIL, user)
                .set(Tables.ORDERLYWEB_REPORT_RUN.DATE, Timestamp.from(date))
                .set(Tables.ORDERLYWEB_REPORT_RUN.REPORT, report)
                .set(Tables.ORDERLYWEB_REPORT_RUN.INSTANCES, Gson().toJson(instances))
                .set(Tables.ORDERLYWEB_REPORT_RUN.PARAMS, Gson().toJson(params))
                .set(Tables.ORDERLYWEB_REPORT_RUN.GIT_BRANCH, gitBranch)
                .set(Tables.ORDERLYWEB_REPORT_RUN.GIT_COMMIT, gitCommit)
                .execute()
        }
    }

    override fun getAllRunningReports(user: String): List<Running>
    {
        JooqContext().use {
            val result = it.dsl.select(
                    ORDERLYWEB_REPORT_RUN.DATE,
                    ORDERLYWEB_REPORT_RUN.REPORT.`as`("name"),
                    ORDERLYWEB_REPORT_RUN.EMAIL,
                    ORDERLYWEB_REPORT_RUN.KEY,
                    ORDERLYWEB_REPORT_RUN.ID
            )
                    .from(ORDERLYWEB_REPORT_RUN)
                    .where(ORDERLYWEB_REPORT_RUN.EMAIL.eq(user))

            // if (result.count() == 0)
            // {
            //     throw UnknownObjectError(user, "getAllRunningReports")
            // }
            // else
            // {
                return result.fetchInto(Running::class.java)
            // }
        }
    }

    override fun getReportRun(key: String): List<ReportRunLog>
    {
        JooqContext().use {
            val result = it.dsl.select(
                    ORDERLYWEB_REPORT_RUN.EMAIL,
                    ORDERLYWEB_REPORT_RUN.DATE,
                    ORDERLYWEB_REPORT_RUN.REPORT,
                    ORDERLYWEB_REPORT_RUN.INSTANCES,
                    ORDERLYWEB_REPORT_RUN.PARAMS,
                    ORDERLYWEB_REPORT_RUN.GIT_BRANCH.`as`("gitBranch"),
                    ORDERLYWEB_REPORT_RUN.GIT_COMMIT.`as`("gitCommit"),
                    ORDERLYWEB_REPORT_RUN.STATUS,
                    ORDERLYWEB_REPORT_RUN.LOGS,
                    ORDERLYWEB_REPORT_RUN.REPORT_VERSION.`as`("reportVersion")
            )
                    .from(ORDERLYWEB_REPORT_RUN)
                    .where(ORDERLYWEB_REPORT_RUN.ID.equals(key))

            if (result.count() == 0)
            {
                throw UnknownObjectError(key, "getReportRun")
            }
            else
            {
                return result.fetchInto(ReportRunLog::class.java)
            }
        }
    }
}
