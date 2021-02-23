package org.vaccineimpact.orderlyweb.db.repositories

import org.jooq.JoinType
import org.jooq.impl.DSL.*
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.db.*
import org.vaccineimpact.orderlyweb.db.Tables.*
import org.vaccineimpact.orderlyweb.errors.UnknownObjectError
import org.vaccineimpact.orderlyweb.models.*
import java.sql.Timestamp
import java.time.Instant

interface ReportRepository
{
    fun getAllReports(): List<Report>
    fun getGlobalPinnedReports(): List<Report>

    @Throws(UnknownObjectError::class)
    fun getReportsByName(name: String): List<String>

    fun togglePublishStatus(name: String, version: String): Boolean

    @Throws(UnknownObjectError::class)
    fun getReportVersion(name: String, version: String): ReportVersionWithDescLatest

    fun getAllReportVersions(): List<ReportVersionWithDescLatest>

    fun getAllCustomFields(): Map<String, String?>

    fun getCustomFieldsForVersions(versionIds: List<String>): Map<String, Map<String, String>>

    fun getParametersForVersions(versionIds: List<String>): Map<String, Map<String, String>>

    fun getDatedChangelogForReport(report: String, latestDate: Instant): List<Changelog>

    fun getLatestVersion(report: String): ReportVersionWithDescLatest

    fun setGlobalPinnedReports(reportNames: List<String>)

    fun reportExists(reportName: String): Boolean

    fun getDrafts(): List<ReportVersionWithChangelogsParams>

    fun getReportsWithPublishStatus(): List<ReportWithPublishStatus>

    fun publish(ids: List<String>)

    fun getLatestReportVersions(reports: List<String>): List<ReportWithDate>

    fun getReportRun(key: String): ReportRunLog
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
                    .select(ORDERLYWEB_REPORT_VERSION_FULL.REPORT.`as`("name"),
                            ORDERLYWEB_REPORT_VERSION_FULL.DISPLAYNAME,
                            ORDERLYWEB_REPORT_VERSION_FULL.ID.`as`("latestVersion"))
                    .from(ORDERLYWEB_REPORT_VERSION_FULL)
                    .join(latestVersionForEachReport.tableName)
                    .on(ORDERLYWEB_REPORT_VERSION_FULL.ID.eq(latestVersionForEachReport.field("latestVersion")))
                    .where(shouldIncludeReportVersion)
                    .orderBy(ORDERLYWEB_REPORT_VERSION_FULL.REPORT)
                    .fetchInto(Report::class.java)
        }
    }

    override fun getReportsByName(name: String): List<String>
    {
        JooqContext().use {

            val result = it.dsl.select(ORDERLYWEB_REPORT_VERSION_FULL.ID)
                    .from(ORDERLYWEB_REPORT_VERSION_FULL)
                    .where(ORDERLYWEB_REPORT_VERSION_FULL.REPORT.eq(name)
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
                    .select(ORDERLYWEB_PINNED_REPORT_GLOBAL.ORDERING,
                            ORDERLYWEB_PINNED_REPORT_GLOBAL.REPORT.`as`("name"),
                            ORDERLYWEB_REPORT_VERSION_FULL.DISPLAYNAME,
                            ORDERLYWEB_REPORT_VERSION_FULL.ID.`as`("latestVersion"))
                    .fromJoinPath(ORDERLYWEB_PINNED_REPORT_GLOBAL, REPORT)
                    .join(ORDERLYWEB_REPORT_VERSION_FULL)
                    .on(ORDERLYWEB_REPORT_VERSION_FULL.REPORT.eq(ORDERLYWEB_PINNED_REPORT_GLOBAL.REPORT))
                    .where(shouldIncludeReportVersion)
                    .and(ORDERLYWEB_REPORT_VERSION_FULL.PUBLISHED.eq(true))
                    .fetch()

            return versions.groupBy { r -> r["name"] }.map {
                it.value.maxBy { r -> r["latestVersion"] as String }
            }.sortedBy { r -> r!![ORDERLYWEB_PINNED_REPORT_GLOBAL.ORDERING] }
                    .map { r -> r!!.into(Report::class.java) }
        }
    }

    override fun getReportVersion(name: String, version: String): ReportVersionWithDescLatest
    {
        //raise exception if version does not belong to named report, or version does not exist
        JooqContext().use {
            return getReportVersion(name, version, it)
        }
    }

    override fun getAllReportVersions(): List<ReportVersionWithDescLatest>
    {
        JooqContext().use {
            // create a temp table containing the latest version ID for each report name
            val latestVersionForEachReport = getLatestVersionsForReports(it)

            return it.dsl.withTemporaryTable(latestVersionForEachReport)
                    .select(ORDERLYWEB_REPORT_VERSION_FULL.REPORT.`as`("name"),
                            ORDERLYWEB_REPORT_VERSION_FULL.DISPLAYNAME,
                            ORDERLYWEB_REPORT_VERSION_FULL.ID,
                            ORDERLYWEB_REPORT_VERSION_FULL.PUBLISHED,
                            ORDERLYWEB_REPORT_VERSION_FULL.DATE,
                            latestVersionForEachReport.field<String>("latestVersion"),
                            ORDERLYWEB_REPORT_VERSION_FULL.DESCRIPTION
                    )
                    .from(ORDERLYWEB_REPORT_VERSION_FULL)
                    .join(latestVersionForEachReport.tableName)
                    .on(ORDERLYWEB_REPORT_VERSION_FULL.REPORT.eq(latestVersionForEachReport.field("report")))
                    .where(shouldIncludeReportVersion)
                    .orderBy(ORDERLYWEB_REPORT_VERSION_FULL.REPORT, ORDERLYWEB_REPORT_VERSION_FULL.ID)
                    .fetchInto(ReportVersionWithDescLatest::class.java)
        }
    }

    override fun togglePublishStatus(name: String, version: String): Boolean
    {
        JooqContext().use {
            val existing = getReportVersion(name, version, it)
            val newStatus = !existing.published

            val rowExists = it.dsl.select(count())
                    .from(ORDERLYWEB_REPORT_VERSION)
                    .where(ORDERLYWEB_REPORT_VERSION.ID.eq(version))
                    .fetchOne(0, Int::class.java)

            if (rowExists > 0)
            {
                it.dsl.update(ORDERLYWEB_REPORT_VERSION)
                        .set(ORDERLYWEB_REPORT_VERSION.PUBLISHED, newStatus)
                        .where(ORDERLYWEB_REPORT_VERSION.ID.eq(version))
                        .execute()
            }
            else
            {
                val record = it.dsl.newRecord(ORDERLYWEB_REPORT_VERSION)
                        .apply {
                            this.id = version
                            this.published = newStatus
                        }
                record.store()
            }
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
            return getParametersForVersions(versionIds, ctx)
        }
    }

    override fun setGlobalPinnedReports(reportNames: List<String>)
    {
        JooqContext().use {
            it.dsl.transaction { config ->
                val dsl = using(config)

                dsl.deleteFrom(ORDERLYWEB_PINNED_REPORT_GLOBAL)
                        .execute()
                reportNames.forEachIndexed { index, reportName ->
                    dsl.insertInto(ORDERLYWEB_PINNED_REPORT_GLOBAL)
                            .set(ORDERLYWEB_PINNED_REPORT_GLOBAL.ORDERING, index)
                            .set(ORDERLYWEB_PINNED_REPORT_GLOBAL.REPORT, reportName)
                            .execute()
                }
            }
        }
    }

    override fun reportExists(reportName: String): Boolean
    {
        JooqContext().use { ctx ->
            return ctx.dsl.selectFrom(REPORT)
                    .where(REPORT.NAME.eq(reportName))
                    .fetch()
                    .count() > 0
        }
    }

    override fun getDatedChangelogForReport(report: String, latestDate: Instant): List<Changelog>
    {
        return JooqContext().use {
            it.dsl.select(changelogReportVersionColumnForUser.`as`("REPORT_VERSION"),
                    CHANGELOG.LABEL,
                    CHANGELOG.VALUE,
                    CHANGELOG.FROM_FILE,
                    CHANGELOG_LABEL.PUBLIC)
                    .fromJoinPath(CHANGELOG, CHANGELOG_LABEL)
                    .join(ORDERLYWEB_REPORT_VERSION_FULL)
                    .on(changelogReportVersionColumnForUser.eq(ORDERLYWEB_REPORT_VERSION_FULL.ID))
                    .where(ORDERLYWEB_REPORT_VERSION_FULL.REPORT.eq(report))
                    .and(ORDERLYWEB_REPORT_VERSION_FULL.DATE.lessOrEqual(Timestamp.from(latestDate)))
                    .and(shouldIncludeChangelogItem)
                    .orderBy(CHANGELOG.ORDERING.desc())
                    .fetchInto(Changelog::class.java)
        }
    }

    override fun getLatestVersion(report: String): ReportVersionWithDescLatest
    {
        JooqContext().use {
            val latestVersionForEachReport = getLatestVersionsForReports(it)

            return it.dsl.withTemporaryTable(latestVersionForEachReport)
                    .select(ORDERLYWEB_REPORT_VERSION_FULL.REPORT.`as`("name"),
                            ORDERLYWEB_REPORT_VERSION_FULL.DISPLAYNAME,
                            ORDERLYWEB_REPORT_VERSION_FULL.ID,
                            ORDERLYWEB_REPORT_VERSION_FULL.PUBLISHED,
                            ORDERLYWEB_REPORT_VERSION_FULL.DATE,
                            latestVersionForEachReport.field<String>("latestVersion"),
                            ORDERLYWEB_REPORT_VERSION_FULL.DESCRIPTION
                    )
                    .from(ORDERLYWEB_REPORT_VERSION_FULL)
                    .join(latestVersionForEachReport.tableName)
                    .on(ORDERLYWEB_REPORT_VERSION_FULL.REPORT.eq(latestVersionForEachReport.field("report")))
                    .where(shouldIncludeReportVersion)
                    .and(ORDERLYWEB_REPORT_VERSION_FULL.REPORT.eq(report))
                    .and(ORDERLYWEB_REPORT_VERSION_FULL.ID.eq(latestVersionForEachReport.field("latestVersion")))
                    .fetchAny()?.into(ReportVersionWithDescLatest::class.java) ?: throw UnknownObjectError(report, "report")

        }
    }

    override fun getDrafts(): List<ReportVersionWithChangelogsParams>
    {
        JooqContext().use {

            val records = it.dsl.select(ORDERLYWEB_REPORT_VERSION_FULL.REPORT,
                    ORDERLYWEB_REPORT_VERSION_FULL.DISPLAYNAME,
                    ORDERLYWEB_REPORT_VERSION_FULL.ID,
                    ORDERLYWEB_REPORT_VERSION_FULL.DATE,
                    ORDERLYWEB_REPORT_VERSION_FULL.DESCRIPTION,
                    CHANGELOG.LABEL,
                    CHANGELOG.VALUE,
                    CHANGELOG.ORDERING,
                    CHANGELOG.FROM_FILE,
                    CHANGELOG_LABEL.PUBLIC)
                    .from(ORDERLYWEB_REPORT_VERSION_FULL)
                    .leftJoin(CHANGELOG)
                    .on(ORDERLYWEB_REPORT_VERSION_FULL.ID.eq(CHANGELOG.REPORT_VERSION))
                    .joinPath(CHANGELOG, CHANGELOG_LABEL, joinType = JoinType.LEFT_OUTER_JOIN)
                    .where(shouldIncludeReportVersion)
                    .and(ORDERLYWEB_REPORT_VERSION_FULL.PUBLISHED.eq(false))
                    .orderBy(ORDERLYWEB_REPORT_VERSION_FULL.REPORT, ORDERLYWEB_REPORT_VERSION_FULL.ID)
                    .fetch()

            val versions = records.groupBy { record -> record[ORDERLYWEB_REPORT_VERSION_FULL.ID] }
            val params = getParametersForVersions(versions.keys, it)

            return versions.map { group ->
                val changelog = group.value
                        .filter { it[CHANGELOG.LABEL] != null }
                        .sortedByDescending { it[CHANGELOG.ORDERING] }
                        .map {
                            Changelog(it[ORDERLYWEB_REPORT_VERSION_FULL.ID],
                                    it[CHANGELOG.LABEL],
                                    it[CHANGELOG.VALUE],
                                    it[CHANGELOG.FROM_FILE],
                                    it[CHANGELOG_LABEL.PUBLIC])
                        }
                val ref = group.value.first()
                ReportVersionWithChangelogsParams(ref[ORDERLYWEB_REPORT_VERSION_FULL.REPORT],
                        ref[ORDERLYWEB_REPORT_VERSION_FULL.DISPLAYNAME],
                        group.key,
                        ref[ORDERLYWEB_REPORT_VERSION_FULL.DATE].toInstant(),
                        false,
                        params[group.key] ?: mapOf(),
                        changelog
                )
            }
        }
    }

    override fun getReportsWithPublishStatus(): List<ReportWithPublishStatus>
    {
        JooqContext().use {
            return it.dsl.selectDistinct(REPORT.NAME,
                    firstValue(ORDERLYWEB_REPORT_VERSION_FULL.DISPLAYNAME)
                            .over()
                            .partitionBy(ORDERLYWEB_REPORT_VERSION_FULL.REPORT)
                            .orderBy(ORDERLYWEB_REPORT_VERSION_FULL.DATE.desc())
                            .`as`("displayName"),
                    ORDERLYWEB_REPORT_VERSION_FULL.PUBLISHED
                            .maxOver()
                            .partitionBy(ORDERLYWEB_REPORT_VERSION_FULL.REPORT)
                            .`as`("hasBeenPublished"))
                    .from(ORDERLYWEB_REPORT_VERSION_FULL)
                    .join(REPORT)
                    .on(ORDERLYWEB_REPORT_VERSION_FULL.REPORT.eq(REPORT.NAME))
                    .orderBy(ORDERLYWEB_REPORT_VERSION_FULL.DATE.desc())
                    .fetchInto(ReportWithPublishStatus::class.java)
        }
    }

    override fun publish(ids: List<String>)
    {
        JooqContext().use {
            it.dsl.update(ORDERLYWEB_REPORT_VERSION)
                    .set(ORDERLYWEB_REPORT_VERSION.PUBLISHED, true)
                    .where(ORDERLYWEB_REPORT_VERSION.ID.`in`(ids))
                    .execute()
        }
    }

    private fun getParametersForVersions(versionIds: Collection<String>, ctx: JooqContext): Map<String, Map<String, String>>
    {
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

    private fun getReportVersion(name: String, version: String, ctx: JooqContext): ReportVersionWithDescLatest
    {
        val latestVersionForEachReport = getLatestVersionsForReports(ctx)

        return ctx.dsl.withTemporaryTable(latestVersionForEachReport)
                .select(ORDERLYWEB_REPORT_VERSION_FULL.REPORT.`as`("name"),
                        ORDERLYWEB_REPORT_VERSION_FULL.DISPLAYNAME,
                        ORDERLYWEB_REPORT_VERSION_FULL.ID,
                        ORDERLYWEB_REPORT_VERSION_FULL.PUBLISHED,
                        ORDERLYWEB_REPORT_VERSION_FULL.DATE,
                        latestVersionForEachReport.field<String>("latestVersion"),
                        ORDERLYWEB_REPORT_VERSION_FULL.DESCRIPTION)
                .from(ORDERLYWEB_REPORT_VERSION_FULL)
                .join(latestVersionForEachReport.tableName)
                .on(ORDERLYWEB_REPORT_VERSION_FULL.REPORT.eq(latestVersionForEachReport.field("report")))
                .where(ORDERLYWEB_REPORT_VERSION_FULL.REPORT.eq(name))
                .and(ORDERLYWEB_REPORT_VERSION_FULL.ID.eq(version))
                .and(shouldIncludeReportVersion)
                .singleOrNull()
                ?.into(ReportVersionWithDescLatest::class.java)
                ?: throw UnknownObjectError("$name-$version", "reportVersion")
    }

    private fun getLatestVersionsForReports(db: JooqContext): TempTable
    {
        return db.dsl.select(
                ORDERLYWEB_REPORT_VERSION_FULL.REPORT,
                ORDERLYWEB_REPORT_VERSION_FULL.ID.`as`("latestVersion"),
                ORDERLYWEB_REPORT_VERSION_FULL.DATE.max().`as`("maxDate")
        )
                .from(ORDERLYWEB_REPORT_VERSION_FULL)
                .where(shouldIncludeReportVersion)
                .groupBy(ORDERLYWEB_REPORT_VERSION_FULL.REPORT)
                .asTemporaryTable(name = "latest_version_for_each_report")
    }

    override fun getLatestReportVersions(reports: List<String>): List<ReportWithDate>
    {
        JooqContext().use {
            return it.dsl.select(
                ORDERLYWEB_REPORT_VERSION_FULL.REPORT.`as`("name"),
                ORDERLYWEB_REPORT_VERSION_FULL.DATE.max().`as`("date")
            )
                .from(ORDERLYWEB_REPORT_VERSION_FULL)
                .where(ORDERLYWEB_REPORT_VERSION_FULL.REPORT.`in`(reports))
                .groupBy(ORDERLYWEB_REPORT_VERSION_FULL.REPORT)
                .fetchInto(ReportWithDate::class.java)
        }
    }

    override fun getReportRun(key: String): ReportRunLog
    {
        JooqContext().use {
            return it.dsl.select(
                    ORDERLYWEB_REPORT_RUN.EMAIL,
                    ORDERLYWEB_REPORT_RUN.DATE,
                    ORDERLYWEB_REPORT_RUN.REPORT,
                    ORDERLYWEB_REPORT_RUN.INSTANCES,
                    ORDERLYWEB_REPORT_RUN.PARAMS,
                    ORDERLYWEB_REPORT_RUN.GIT_BRANCH.`as`("gitBranch"),
                    ORDERLYWEB_REPORT_RUN.GIT_COMMIT.`as`("gitCommit"),
                    ORDERLYWEB_REPORT_RUN.STATUS,
                    ORDERLYWEB_REPORT_RUN.LOGS,
                    ORDERLYWEB_REPORT_RUN.REPORT_VERSION.`as`("reportVersion"))
                    .from(ORDERLYWEB_REPORT_RUN)
                    .where(ORDERLYWEB_REPORT_RUN.KEY.eq(key))
                    .singleOrNull()
                    ?.into(ReportRunLog::class.java)
                    ?: throw UnknownObjectError("key", "getReportRun")
        }
    }

    // shouldInclude for the relational schema
    private val shouldIncludeReportVersion =
            (ORDERLYWEB_REPORT_VERSION_FULL.REPORT.`in`(reportReadingScopes).or(isGlobalReader.or(isReviewer)))
                    .and(ORDERLYWEB_REPORT_VERSION_FULL.PUBLISHED.bitOr(isReviewer))

    private val shouldIncludeChangelogItem =
            if (isReviewer)
                trueCondition()
            else
                CHANGELOG_LABEL.PUBLIC.isTrue.and(CHANGELOG.REPORT_VERSION_PUBLIC.isNotNull)

    private val changelogReportVersionColumnForUser =
            if (isReviewer)
                CHANGELOG.REPORT_VERSION
            else
                CHANGELOG.REPORT_VERSION_PUBLIC
}
