package org.vaccineimpact.orderlyweb.db.repositories

import com.google.gson.Gson
import org.vaccineimpact.orderlyweb.db.*
import org.vaccineimpact.orderlyweb.db.Tables.ORDERLYWEB_REPORT_RUN
import org.vaccineimpact.orderlyweb.models.ReportRunWithDate
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
    fun getAllReportRunsForUser(user: String): List<ReportRunWithDate>
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

    override fun getAllReportRunsForUser(user: String): List<ReportRunWithDate>
    {
        JooqContext().use {
            val result = it.dsl.select(
                    ORDERLYWEB_REPORT_RUN.REPORT.`as`("name"),
                    ORDERLYWEB_REPORT_RUN.KEY,
                    ORDERLYWEB_REPORT_RUN.DATE
            )
                    .from(ORDERLYWEB_REPORT_RUN)
                    .where(ORDERLYWEB_REPORT_RUN.EMAIL.eq(user))

            return result.fetchInto(ReportRunWithDate::class.java)
        }
    }
}
