package org.vaccineimpact.orderlyweb.db.repositories

import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.Tables
import org.vaccineimpact.orderlyweb.db.Tables.ORDERLYWEB_REPORT_TAG
import org.vaccineimpact.orderlyweb.db.Tables.ORDERLYWEB_REPORT_VERSION_TAG

interface TagRepository
{
    fun getAllTags(): List<String>
    fun getReportTags(reportNames: List<String>): Map<String, List<String>>
    fun tagReport(reportName: String, tag: String)
    fun tagVersion(versionId: String, tag: String)
}

class OrderlyWebTagRepository : TagRepository
{
    override fun getAllTags(): List<String> {
        JooqContext().use {
            return it.dsl.select(
                            Tables.ORDERLYWEB_REPORT_TAG.TAG)
                    .from(Tables.ORDERLYWEB_REPORT_TAG)
                    .union(it.dsl.select(Tables.ORDERLYWEB_REPORT_VERSION_TAG.TAG)
                            .from(Tables.ORDERLYWEB_REPORT_VERSION_TAG))
                    .fetchInto(String::class.java)
                    .sorted()
        }
    }

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
            it.dsl.insertInto(ORDERLYWEB_REPORT_TAG,
                    ORDERLYWEB_REPORT_TAG.REPORT,
                    ORDERLYWEB_REPORT_TAG.TAG)
                    .values(reportName, tag)
                    .onDuplicateKeyIgnore()
                    .execute()
        }
    }

    override fun tagVersion(versionId: String, tag: String)
    {
        JooqContext().use {
            it.dsl.insertInto(ORDERLYWEB_REPORT_VERSION_TAG,
                    ORDERLYWEB_REPORT_VERSION_TAG.REPORT_VERSION,
                    ORDERLYWEB_REPORT_VERSION_TAG.TAG)
                    .values(versionId, tag)
                    .onDuplicateKeyIgnore()
                    .execute()
        }
    }
}
