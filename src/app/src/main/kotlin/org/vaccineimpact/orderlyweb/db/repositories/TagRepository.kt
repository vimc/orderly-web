package org.vaccineimpact.orderlyweb.db.repositories

import org.jooq.impl.DSL
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.Tables.ORDERLYWEB_REPORT_TAG
import org.vaccineimpact.orderlyweb.db.Tables.ORDERLYWEB_REPORT_VERSION_TAG
import org.vaccineimpact.orderlyweb.models.ReportVersionTags

interface TagRepository
{
    fun getReportTags(reportNames: List<String>): Map<String, List<String>>
    fun updateTags(reportName: String, versionId: String, tags: ReportVersionTags)
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

    override fun updateTags(reportName: String, versionId: String, tags: ReportVersionTags)
    {
        JooqContext().use {
            it.dsl.transaction { config ->
                val dsl = DSL.using(config)
                dsl.deleteFrom(ORDERLYWEB_REPORT_TAG)
                        .where(ORDERLYWEB_REPORT_TAG.REPORT.eq(reportName))
                        .execute()

                for (tag in tags.reportTags)
                {
                    dsl.insertInto(ORDERLYWEB_REPORT_TAG,
                            ORDERLYWEB_REPORT_TAG.REPORT,
                            ORDERLYWEB_REPORT_TAG.TAG)
                            .values(reportName, tag)
                            .onDuplicateKeyIgnore()
                            .execute()
                }

                dsl.deleteFrom(ORDERLYWEB_REPORT_VERSION_TAG)
                        .where(ORDERLYWEB_REPORT_VERSION_TAG.REPORT_VERSION.eq(versionId))
                        .execute()

                for (tag in tags.versionTags)
                {
                    dsl.insertInto(ORDERLYWEB_REPORT_VERSION_TAG,
                            ORDERLYWEB_REPORT_VERSION_TAG.REPORT_VERSION,
                            ORDERLYWEB_REPORT_TAG.TAG)
                            .values(versionId, tag)
                            .onDuplicateKeyIgnore()
                            .execute()
                }
            }
        }
    }
}
