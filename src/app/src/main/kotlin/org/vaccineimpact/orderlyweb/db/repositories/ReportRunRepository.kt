package org.vaccineimpact.orderlyweb.db.repositories

import com.google.gson.Gson
import org.vaccineimpact.orderlyweb.db.*
import org.vaccineimpact.orderlyweb.errors.UnknownObjectError
import org.vaccineimpact.orderlyweb.models.ReportRunLog
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
    @Throws(UnknownObjectError::class)
    fun getReportRun(key: String): ReportRunLog
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

    override fun getReportRun(key: String): ReportRunLog
    {
        JooqContext().use {
            return it.dsl.select(
                    Tables.ORDERLYWEB_REPORT_RUN.EMAIL.`as`("email"),
                    Tables.ORDERLYWEB_REPORT_RUN.DATE.`as`("date"),
                    Tables.ORDERLYWEB_REPORT_RUN.REPORT.`as`("report"),
                    Tables.ORDERLYWEB_REPORT_RUN.INSTANCES.`as`("instances"),
                    Tables.ORDERLYWEB_REPORT_RUN.PARAMS.`as`("params"),
                    Tables.ORDERLYWEB_REPORT_RUN.GIT_BRANCH.`as`("gitBranch"),
                    Tables.ORDERLYWEB_REPORT_RUN.GIT_COMMIT.`as`("gitCommit"),
                    Tables.ORDERLYWEB_REPORT_RUN.STATUS.`as`("status"),
                    Tables.ORDERLYWEB_REPORT_RUN.LOGS.`as`("logs"),
                    Tables.ORDERLYWEB_REPORT_RUN.REPORT_VERSION.`as`("reportVersion"))
                    .from(Tables.ORDERLYWEB_REPORT_RUN)
                    .where(Tables.ORDERLYWEB_REPORT_RUN.KEY.eq(key))
                    .singleOrNull()
                    ?.into(ReportRunLog::class.java)
                    ?: throw UnknownObjectError("key", "getReportRun")
        }
    }
}
