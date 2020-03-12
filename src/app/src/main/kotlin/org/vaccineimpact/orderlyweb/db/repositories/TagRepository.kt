package org.vaccineimpact.orderlyweb.db.repositories

import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.Tables.*

interface TagRepository
{
    fun getReportTags(reportNames: List<String>): Map<String, List<String>>
    fun deleteReportTag(reportName: String, tag: String)
    fun deleteVersionTag(versionId: String, tag: String)

    fun tagReport(reportName: String, tags: List<String>)
    fun tagVersion(versionId: String, tags: List<String>)
}

class OrderlyWebTagRepository : TagRepository
{
    override fun getReportTags(reportNames: List<String>): Map<String, List<String>>
    {
        JooqContext().use { ctx ->
            return ctx.dsl.select(
                            ORDERLYWEB_REPORT_TAG.REPORT,
                            ORDERLYWEB_REPORT_TAG.TAG)
                    .from(ORDERLYWEB_REPORT_TAG)
                    .where(ORDERLYWEB_REPORT_TAG.REPORT.`in`(reportNames))
                    .groupBy { it[ORDERLYWEB_REPORT_TAG.REPORT] }
                    .mapValues { it.value.map { r -> r[ORDERLYWEB_REPORT_TAG.TAG] }.sorted() }
        }
    }

    override fun tagReport(reportName: String, tags: List<String>)
    {
        JooqContext().use {
            for (tag in tags)
            {
                it.dsl.insertInto(ORDERLYWEB_REPORT_TAG,
                                ORDERLYWEB_REPORT_TAG.REPORT,
                                ORDERLYWEB_REPORT_TAG.TAG)
                        .values(reportName, tag)
                        .onDuplicateKeyIgnore()
                        .execute()
            }
        }
    }

    override fun tagVersion(versionId: String, tags: List<String>)
    {
        JooqContext().use {
            for (tag in tags)
            {
                it.dsl.insertInto(ORDERLYWEB_REPORT_VERSION_TAG,
                                ORDERLYWEB_REPORT_VERSION_TAG.REPORT_VERSION,
                                ORDERLYWEB_REPORT_VERSION_TAG.TAG)
                        .values(versionId, tag)
                        .onDuplicateKeyIgnore()
                        .execute()
            }
        }
    }

    override fun deleteReportTag(reportName: String, tag: String)
    {
        JooqContext().use {
            it.dsl.deleteFrom(ORDERLYWEB_REPORT_TAG)
                    .where(ORDERLYWEB_REPORT_TAG.REPORT.eq(reportName))
                    .and(ORDERLYWEB_REPORT_TAG.TAG.eq(tag))
                    .execute()
        }
    }

    override fun deleteVersionTag(versionId: String, tag: String)
    {
        JooqContext().use {
            it.dsl.deleteFrom(ORDERLYWEB_REPORT_VERSION_TAG)
                    .where(ORDERLYWEB_REPORT_VERSION_TAG.REPORT_VERSION.eq(versionId))
                    .and(ORDERLYWEB_REPORT_VERSION_TAG.TAG.eq(tag))
                    .execute()
        }
    }
}
