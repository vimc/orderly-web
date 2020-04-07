package org.vaccineimpact.orderlyweb.db.repositories

import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.db.*
import org.vaccineimpact.orderlyweb.db.Tables.*
import org.vaccineimpact.orderlyweb.errors.UnknownObjectError
import org.vaccineimpact.orderlyweb.models.BasicReportVersion
import org.vaccineimpact.orderlyweb.models.Report

interface ReportRepository
{
    fun getAllReports(): List<Report>

    fun getAllReportVersions(): List<BasicReportVersion>

    fun getGlobalPinnedReports(): List<Report>

    @Throws(UnknownObjectError::class)
    fun getReportsByName(name: String): List<String>

    fun getReportVersion(name: String, version: String): BasicReportVersion

    fun togglePublishStatus(name: String, version: String): Boolean

    fun getCustomFields(): Map<String, String?>

    fun getCustomFieldsForVersions(versionIds: List<String>): Map<String, List<Pair<String, String>>>
}

class OrderlyReportRepository(val isReviewer: Boolean,
                              val isGlobalReader: Boolean,
                              val reportReadingScopes: List<String> = listOf()) : ReportRepository
{
    constructor(context: ActionContext) : this(context.isReviewer(), context.isGlobalReader(), context.reportReadingScopes)

    override fun getAllReportVersions(): List<BasicReportVersion>
    {
        JooqContext().use {
            // create a temp table containing the latest version ID for each report name
            val latestVersionForEachReport = getLatestVersionsForReports(it)

            return it.dsl.withTemporaryTable(latestVersionForEachReport)
                    .select(REPORT_VERSION.REPORT.`as`("name"),
                            REPORT_VERSION.DISPLAYNAME,
                            REPORT_VERSION.ID,
                            REPORT_VERSION.PUBLISHED,
                            REPORT_VERSION.DATE,
                            latestVersionForEachReport.field<String>("latestVersion"),
                            REPORT_VERSION.DESCRIPTION
                    )
                    .from(REPORT_VERSION)
                    .join(latestVersionForEachReport.tableName)
                    .on(REPORT_VERSION.REPORT.eq(latestVersionForEachReport.field("report")))
                    .where(shouldIncludeReportVersion)
                    .orderBy(REPORT_VERSION.REPORT, REPORT_VERSION.ID)
                    .fetchInto(BasicReportVersion::class.java)
        }
    }

    override fun getGlobalPinnedReports(): List<Report>
    {
        JooqContext().use {

            val versions = it.dsl
                    .select(ORDERLYWEB_PINNED_REPORT_GLOBAL.ORDERING,
                            ORDERLYWEB_PINNED_REPORT_GLOBAL.REPORT,
                            REPORT_VERSION.DISPLAYNAME,
                            REPORT_VERSION.ID.`as`("latestVersion"))
                    .fromJoinPath(ORDERLYWEB_PINNED_REPORT_GLOBAL, REPORT)
                    .join(REPORT_VERSION)
                    .on(REPORT_VERSION.REPORT.eq(REPORT.NAME))
                    .fetch()

            return versions.groupBy { it[REPORT_VERSION.REPORT] }.map {
                it.value.minBy { it[REPORT_VERSION.ID] }!!.into(Report::class.java)
            }
        }
    }

    override fun getAllReports(): List<Report>
    {
        JooqContext().use {

            // create a temp table containing the latest version ID for each report name
            val latestVersionForEachReport = getLatestVersionsForReports(it)

            return it.dsl.withTemporaryTable(latestVersionForEachReport)
                    .select(REPORT_VERSION.REPORT.`as`("name"),
                            REPORT_VERSION.DISPLAYNAME,
                            REPORT_VERSION.ID.`as`("latestVersion"))
                    .from(REPORT_VERSION)
                    .join(latestVersionForEachReport.tableName)
                    .on(REPORT_VERSION.ID.eq(latestVersionForEachReport.field("latestVersion")))
                    .where(shouldIncludeReportVersion)
                    .orderBy(REPORT_VERSION.REPORT)
                    .fetchInto(Report::class.java)
        }
    }

    override fun getReportsByName(name: String): List<String>
    {
        JooqContext().use {

            val result = it.dsl.select(REPORT_VERSION.ID)
                    .from(REPORT_VERSION)
                    .where(REPORT_VERSION.REPORT.eq(name)
                            .and(shouldIncludeReportVersion))

            if (result.count() == 0)
            {
                throw UnknownObjectError(name, "report")
            }
            else
            {
                return result.fetchInto(String::class.java)
            }
        }
    }

    override fun togglePublishStatus(name: String, version: String): Boolean
    {
        JooqContext().use {
            val existing = getReportVersion(it, name, version)
            val newStatus = !existing.published
            it.dsl.update(REPORT_VERSION)
                    .set(REPORT_VERSION.PUBLISHED, newStatus)
                    .where(REPORT_VERSION.ID.eq(version))
                    .execute()

            return newStatus
        }
    }

    override fun getReportVersion(name: String, version: String): BasicReportVersion
    {
        //raise exception if version does not belong to named report, or version does not exist
        JooqContext().use {
            return getReportVersion(it, name, version)
        }
    }

    override fun getCustomFields(): Map<String, String?>
    {
        JooqContext().use {
            return it.dsl.select(
                    CUSTOM_FIELDS.ID)
                    .from(CUSTOM_FIELDS)
                    .fetch()
                    .associate { r -> r[CUSTOM_FIELDS.ID] to null as String? }
        }
    }

    override fun getCustomFieldsForVersions(versionIds: List<String>): Map<String, List<Pair<String, String>>>
    {
        JooqContext().use {
            return it.dsl.select(
                    REPORT_VERSION_CUSTOM_FIELDS.KEY,
                    REPORT_VERSION_CUSTOM_FIELDS.VALUE,
                    REPORT_VERSION_CUSTOM_FIELDS.REPORT_VERSION)
                    .from(REPORT_VERSION_CUSTOM_FIELDS)
                    .where(REPORT_VERSION_CUSTOM_FIELDS.REPORT_VERSION.`in`(versionIds))
                    .fetch()
                    .groupBy { it[REPORT_VERSION_CUSTOM_FIELDS.REPORT_VERSION] }
                    .mapValues { it.value.map { Pair(it[REPORT_VERSION_CUSTOM_FIELDS.KEY], it[REPORT_VERSION_CUSTOM_FIELDS.VALUE]) } }
        }
    }

    private fun getReportVersion(db: JooqContext, name: String, version: String): BasicReportVersion
    {
        val latestVersionForEachReport = getLatestVersionsForReports(db)

        return db.dsl.withTemporaryTable(latestVersionForEachReport)
                .select(REPORT_VERSION.REPORT.`as`("name"),
                REPORT_VERSION.DISPLAYNAME,
                REPORT_VERSION.ID,
                REPORT_VERSION.PUBLISHED,
                REPORT_VERSION.DATE,
                        latestVersionForEachReport.field<String>("latestVersion"),
                REPORT_VERSION.DESCRIPTION)
                .from(REPORT_VERSION)
                .join(latestVersionForEachReport.tableName)
                .on(REPORT_VERSION.REPORT.eq(latestVersionForEachReport.field("report")))
                .where(REPORT_VERSION.REPORT.eq(name))
                .and(REPORT_VERSION.ID.eq(version))
                .and(shouldIncludeReportVersion)
                .singleOrNull()
                ?.into(BasicReportVersion::class.java)
                ?: throw UnknownObjectError("$name-$version", "reportVersion")

    }

    private fun getLatestVersionsForReports(db: JooqContext): TempTable
    {
        return db.dsl.select(
                REPORT_VERSION.REPORT,
                REPORT_VERSION.ID.`as`("latestVersion"),
                REPORT_VERSION.DATE.max().`as`("maxDate")
        )
                .from(REPORT_VERSION)
                .where(shouldIncludeReportVersion)
                .groupBy(REPORT_VERSION.REPORT)
                .asTemporaryTable(name = "latest_version_for_each_report")
    }

    private val shouldIncludeReportVersion =
            (REPORT_VERSION.REPORT.`in`(reportReadingScopes).or(isGlobalReader.or(isReviewer)))
                    .and(REPORT_VERSION.PUBLISHED.bitOr(isReviewer))

}
