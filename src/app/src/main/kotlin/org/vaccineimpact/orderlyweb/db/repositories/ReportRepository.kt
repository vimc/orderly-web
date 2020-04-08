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
    fun getGlobalPinnedReports(): List<Report>

    @Throws(UnknownObjectError::class)
    fun getReportsByName(name: String): List<String>

    fun togglePublishStatus(name: String, version: String): Boolean

    @Throws(UnknownObjectError::class)
    fun getReportVersion(name: String, version: String): BasicReportVersion

    fun getAllReportVersions(): List<BasicReportVersion>

    fun getCustomFieldsForVersions(versionIds: List<String>): Map<String, Map<String, String>>

    fun getAllCustomFields(): Map<String, String?>
    fun getParametersForVersions(versionIds: List<String>): Map<String, Map<String, String>>
}

class OrderlyReportRepository(val isReviewer: Boolean,
                              val isGlobalReader: Boolean,
                              reportReadingScopes: List<String> = listOf()) : ReportRepository
{

    constructor(context: ActionContext) : this(context.isReviewer(), context.isGlobalReader(), context.reportReadingScopes)

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

    override fun getGlobalPinnedReports(): List<Report>
    {
        JooqContext().use {

            val versions = it.dsl
                    .select(Tables.ORDERLYWEB_PINNED_REPORT_GLOBAL.ORDERING,
                            Tables.ORDERLYWEB_PINNED_REPORT_GLOBAL.REPORT.`as`("name"),
                            REPORT_VERSION.DISPLAYNAME,
                            REPORT_VERSION.ID.`as`("latestVersion"))
                    .fromJoinPath(Tables.ORDERLYWEB_PINNED_REPORT_GLOBAL, REPORT)
                    .join(REPORT_VERSION)
                    .on(REPORT_VERSION.REPORT.eq(Tables.ORDERLYWEB_PINNED_REPORT_GLOBAL.REPORT))
                    .where(shouldIncludeReportVersion)
                    .fetch()

            return versions.groupBy { r -> r["name"] }.map {
                it.value.maxBy { r -> r["latestVersion"] as String }
            }.sortedBy { r -> r!![Tables.ORDERLYWEB_PINNED_REPORT_GLOBAL.ORDERING] }
                    .map { r -> r!!.into(Report::class.java) }
        }
    }

    override fun getReportVersion(name: String, version: String): BasicReportVersion
    {
        //raise exception if version does not belong to named report, or version does not exist
        JooqContext().use {
            return getReportVersion(name, version, it)
        }
    }

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

    override fun togglePublishStatus(name: String, version: String): Boolean
    {
        JooqContext().use {
            val existing = getReportVersion(name, version, it)
            val newStatus = !existing.published
            it.dsl.update(REPORT_VERSION)
                    .set(REPORT_VERSION.PUBLISHED, newStatus)
                    .where(REPORT_VERSION.ID.eq(version))
                    .execute()

            return newStatus
        }
    }

    override fun getAllCustomFields(): Map<String, String?>
    {
        JooqContext().use {
            return it.dsl.select(
                    CUSTOM_FIELDS.ID)
                    .from(CUSTOM_FIELDS)
                    .fetch()
                    .associate { r -> r[CUSTOM_FIELDS.ID] to null as String? }
        }
    }

    override fun getCustomFieldsForVersions(versionIds: List<String>): Map<String, Map<String, String>>
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
                    .mapValues { it.value.associate { it[REPORT_VERSION_CUSTOM_FIELDS.KEY] to it[REPORT_VERSION_CUSTOM_FIELDS.VALUE] } }
        }
    }

    override fun getParametersForVersions(versionIds: List<String>): Map<String, Map<String, String>>
    {
        JooqContext().use { ctx ->
            return ctx.dsl.select(
                    PARAMETERS.REPORT_VERSION,
                    PARAMETERS.NAME,
                    PARAMETERS.VALUE)
                    .from(PARAMETERS)
                    .where(PARAMETERS.REPORT_VERSION.`in`(versionIds))
                    .fetch()
                    .groupBy { it[PARAMETERS.REPORT_VERSION] }
                    .mapValues { it.value.associate { r -> r[PARAMETERS.NAME] to r[PARAMETERS.VALUE] } }
        }
    }

    private fun getReportVersion(name: String, version: String, ctx: JooqContext): BasicReportVersion
    {
        val latestVersionForEachReport = getLatestVersionsForReports(ctx)

        return ctx.dsl.withTemporaryTable(latestVersionForEachReport)
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

    // shouldInclude for the relational schema
    private val shouldIncludeReportVersion =
            (REPORT_VERSION.REPORT.`in`(reportReadingScopes).or(isGlobalReader.or(isReviewer)))
                    .and(REPORT_VERSION.PUBLISHED.bitOr(isReviewer))

}