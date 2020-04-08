package org.vaccineimpact.orderlyweb.db.repositories

import org.jooq.Record2
import org.jooq.SelectConditionStep
import org.jooq.impl.DSL
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.Tables.*
import org.vaccineimpact.orderlyweb.models.ReportVersionTags


interface TagRepository
{
    fun getAllTags(): List<String>
    fun getReportTags(reportNames: List<String>): Map<String, List<String>>
    fun updateTags(reportName: String, versionId: String, tags: ReportVersionTags)
}

class OrderlyWebTagRepository : TagRepository
{
    override fun getAllTags(): List<String>
    {
        JooqContext().use {
            return it.dsl.select(
                    ORDERLYWEB_REPORT_TAG.TAG)
                    .from(ORDERLYWEB_REPORT_TAG)
                    .union(it.dsl.select(ORDERLYWEB_REPORT_VERSION_TAG.TAG)
                            .from(ORDERLYWEB_REPORT_VERSION_TAG))
                    .union(it.dsl.select(TAG.ID)
                            .from(TAG))
                    .fetchInto(String::class.java)
                    .distinct()
                    .sorted()
        }
    }

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

    override fun getAllTagsForVersions(versionIds: List<String>): Map<String, List<String>>
    {
        JooqContext().use { ctx ->
            return ctx.dsl.select(
                    ORDERLYWEB_REPORT_VERSION_TAG.REPORT_VERSION,
                    ORDERLYWEB_REPORT_VERSION_TAG.TAG)
                    .from(ORDERLYWEB_REPORT_VERSION_TAG)
                    .where(ORDERLYWEB_REPORT_VERSION_TAG.REPORT_VERSION.`in`(versionIds))
                    .groupBy { it[ORDERLYWEB_REPORT_VERSION_TAG.REPORT_VERSION] }
                    .mapValues { it.value.map { r -> r[ORDERLYWEB_REPORT_VERSION_TAG.TAG] } }
        }
    }

    private fun getVersionTagsQuery(versionIds: List<String>, ctx: JooqContext)
            : SelectConditionStep<Record2<String, String>>
    {
        return ctx.dsl.select(
                ORDERLYWEB_REPORT_VERSION_TAG.REPORT_VERSION.`as`("version"),
                ORDERLYWEB_REPORT_VERSION_TAG.TAG)
                .from(ORDERLYWEB_REPORT_VERSION_TAG)
                .where(ORDERLYWEB_REPORT_VERSION_TAG.REPORT_VERSION.`in`(versionIds))
    }

    private fun getReportTagsForVersionsQuery(versionIds: List<String>, ctx: JooqContext)
            : SelectConditionStep<Record2<String, String>>
    {
        return ctx.dsl.select(
                ORDERLYWEB_REPORT_TAG.TAG,
                REPORT_VERSION.ID.`as`("version"))
                .from(ORDERLYWEB_REPORT_TAG)
                .innerJoin(REPORT_VERSION)
                .on(ORDERLYWEB_REPORT_TAG.REPORT.eq(REPORT_VERSION.REPORT))
                .where(REPORT_VERSION.ID.`in`(versionIds))
    }

    private fun getOrderlyTagsForVersionsQuery(versionIds: List<String>, ctx: JooqContext)
            : SelectConditionStep<Record2<String, String>>
    {
        return ctx.dsl.select(
                REPORT_VERSION_TAG.REPORT_VERSION.`as`("version"),
                REPORT_VERSION_TAG.TAG)
                .from(REPORT_VERSION_TAG)
                .where(REPORT_VERSION_TAG.REPORT_VERSION.`in`(versionIds))

    }
}
