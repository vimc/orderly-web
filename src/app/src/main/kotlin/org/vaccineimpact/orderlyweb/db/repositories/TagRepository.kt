package org.vaccineimpact.orderlyweb.db.repositories

import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.Tables

interface TagRepository
{
    fun getReportTags(reportNames: List<String>): Map<String, List<String>>
    fun tagReport(reportName: String, tag: String)
    fun tagVersion(versionId: String, tag: String)
}

class OrderlyTagRepository : TagRepository
{
    override fun getReportTags(reportNames: List<String>): Map<String, List<String>>
    {
        JooqContext().use { ctx ->
            return ctx.dsl.select(
                    Tables.ORDERLYWEB_REPORT_TAG.REPORT,
                    Tables.ORDERLYWEB_REPORT_TAG.TAG)
                    .from(Tables.ORDERLYWEB_REPORT_TAG)
                    .where(Tables.ORDERLYWEB_REPORT_TAG.REPORT.`in`(reportNames))
                    .groupBy { it[Tables.ORDERLYWEB_REPORT_TAG.REPORT] }
                    .mapValues { it.value.map { r -> r[Tables.ORDERLYWEB_REPORT_TAG.TAG] }.sorted() }
        }
    }

    override fun tagReport(reportName: String, tag: String)
    {
        JooqContext().use {
            it.dsl.newRecord(Tables.ORDERLYWEB_REPORT_TAG)
                    .apply {
                        this.report = reportName
                        this.tag = tag
                    }.store()
        }
    }

    override fun tagVersion(versionId: String, tag: String)
    {
        JooqContext().use {
            it.dsl.newRecord(Tables.ORDERLYWEB_REPORT_VERSION_TAG)
                    .apply {
                        this.reportVersion = versionId
                        this.tag = tag
                    }.store()
        }
    }
}
