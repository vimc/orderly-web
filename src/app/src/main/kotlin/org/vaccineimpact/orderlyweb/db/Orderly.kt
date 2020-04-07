package org.vaccineimpact.orderlyweb.db

import org.jooq.impl.DSL.trueCondition
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.db.Tables.*
import org.vaccineimpact.orderlyweb.db.tables.records.ReportVersionRecord
import org.vaccineimpact.orderlyweb.errors.UnknownObjectError
import org.vaccineimpact.orderlyweb.models.*
import java.sql.Timestamp

class Orderly(val isReviewer: Boolean,
              val isGlobalReader: Boolean,
              val reportReadingScopes: List<String> = listOf()) : OrderlyClient
{
    constructor(context: ActionContext) : this(context.isReviewer(), context.isGlobalReader(), context.reportReadingScopes)

    override fun getReportVersionTags(name: String, version: String): ReportVersionTags
    {
        JooqContext().use { ctx ->
            getReportVersion(name, version, ctx)
            val versionTags = getVersionTags(listOf(version))[version] ?: listOf()
            val reportTags = getReportTagsForVersions(listOf(version))[version] ?: listOf()
            val orderlyTags = getOrderlyTags(listOf(version))[version] ?: listOf()
            return ReportVersionTags(versionTags.sorted(), reportTags.sorted(), orderlyTags.sorted())
        }
    }

    override fun getData(name: String, version: String): Map<String, String>
    {
        JooqContext().use {
            getReportVersion(name, version, it)
            return it.dsl.select(
                    REPORT_VERSION_DATA.NAME,
                    REPORT_VERSION_DATA.HASH)
                    .from(REPORT_VERSION_DATA)
                    .where(REPORT_VERSION_DATA.REPORT_VERSION.eq(version))
                    .fetch()
                    .associate { it[REPORT_VERSION_DATA.NAME] to it[REPORT_VERSION_DATA.HASH] }
        }
    }

    override fun getDatum(name: String, version: String, datumname: String): String
    {
        val data = getData(name, version)
        return data[datumname] ?: throw UnknownObjectError(datumname, "Data")
    }

    override fun getResourceHashes(name: String, version: String): Map<String, String>
    {
        return JooqContext().use { ctx ->
            getReportVersion(name, version, ctx)
            ctx.dsl.select(FILE_INPUT.FILENAME, FILE_INPUT.FILE_HASH)
                    .from(FILE_INPUT)
                    .where(FILE_INPUT.REPORT_VERSION.eq(version))
                    .and(FILE_INPUT.FILE_PURPOSE.eq(FilePurpose.RESOURCE.toString()))
                    .fetch()
                    .associate { it[FILE_INPUT.FILENAME] to it[FILE_INPUT.FILE_HASH] }
        }
    }

    override fun getResourceHash(name: String, version: String, resourcename: String): String
    {
        return getResourceHashes(name, version)[resourcename]
                ?: throw UnknownObjectError(resourcename, "Resource")
    }

    override fun getReadme(name: String, version: String): Map<String, String>
    {
        return JooqContext().use { ctx ->
            getReportVersion(name, version, ctx)
            ctx.dsl.select(FILE_INPUT.FILENAME, FILE_INPUT.FILE_HASH)
                    .from(FILE_INPUT)
                    .where(FILE_INPUT.REPORT_VERSION.eq(version))
                    .and(FILE_INPUT.FILE_PURPOSE.eq(FilePurpose.README.toString()))
                    .fetch()
                    .associate { it[FILE_INPUT.FILENAME] to it[FILE_INPUT.FILE_HASH] }
        }
    }

    override fun getLatestChangelogByName(name: String): List<Changelog>
    {
        JooqContext().use {

            val latestVersionDate = it.dsl
                    .select(REPORT_VERSION.DATE)
                    .from(REPORT_VERSION)
                    .join(REPORT)
                    .on(REPORT_VERSION.REPORT.eq(REPORT.NAME))
                    .where(REPORT.NAME.eq(name))
                    .and(shouldIncludeReportVersion)
                    .orderBy(REPORT_VERSION.DATE.desc())
                    .fetchAny()
                    ?: throw UnknownObjectError(name, "report")

            return getDatedChangelogForReport(name, latestVersionDate.value1(), it)

        }
    }

    override fun getChangelogByNameAndVersion(name: String, version: String): List<Changelog>
    {
        JooqContext().use {

            val thisVersion = getReportVersion(name, version, it)
            return getDatedChangelogForReport(thisVersion.report, thisVersion.date, it)

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

    override fun getParameters(version: String): Map<String, String>
    {
        JooqContext().use { ctx ->
            return ctx.dsl.select(
                    PARAMETERS.NAME,
                    PARAMETERS.VALUE)
                    .from(PARAMETERS)
                    .where(PARAMETERS.REPORT_VERSION.eq(version))
                    .fetch()
                    .associate { r -> r[PARAMETERS.NAME] to r[PARAMETERS.VALUE] }
        }
    }

    override fun getVersionTags(versionIds: List<String>): Map<String, List<String>>
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

    override fun getReportTagsForVersions(versionIds: List<String>): Map<String, List<String>>
    {
        JooqContext().use { ctx ->
            return ctx.dsl.select(
                    ORDERLYWEB_REPORT_TAG.TAG,
                    REPORT_VERSION.ID)
                    .from(ORDERLYWEB_REPORT_TAG)
                    .innerJoin(REPORT_VERSION)
                    .on(ORDERLYWEB_REPORT_TAG.REPORT.eq(REPORT_VERSION.REPORT))
                    .where(REPORT_VERSION.ID.`in`(versionIds))
                    .groupBy { it[REPORT_VERSION.ID] }
                    .mapValues { it.value.map { r -> r[ORDERLYWEB_REPORT_TAG.TAG] } }
        }
    }

    override fun getOrderlyTags(versionIds: List<String>): Map<String, List<String>>
    {
        JooqContext().use { ctx ->
            return ctx.dsl.select(
                    REPORT_VERSION_TAG.REPORT_VERSION,
                    REPORT_VERSION_TAG.TAG)
                    .from(REPORT_VERSION_TAG)
                    .where(REPORT_VERSION_TAG.REPORT_VERSION.`in`(versionIds))
                    .groupBy { it[REPORT_VERSION_TAG.REPORT_VERSION] }
                    .mapValues { it.value.map { r -> r[REPORT_VERSION_TAG.TAG] } }
        }
    }

    override fun getDataInfo(name: String, version: String): List<DataInfo>
    {
        JooqContext().use {
            getReportVersion(name, version, it)
            return it.dsl.select(
                    REPORT_VERSION_DATA.NAME,
                    DATA.SIZE_CSV,
                    DATA.SIZE_RDS)
                    .from(REPORT_VERSION_DATA)
                    .innerJoin(DATA)
                    .on(REPORT_VERSION_DATA.HASH.eq(DATA.HASH))
                    .where(REPORT_VERSION_DATA.REPORT_VERSION.eq(version))
                    .fetch()
                    .map { r -> DataInfo(r[REPORT_VERSION_DATA.NAME], r[DATA.SIZE_CSV], r[DATA.SIZE_RDS]) }
        }
    }

    override fun getResourceFiles(name: String, version: String): List<FileInfo>
    {
        return JooqContext().use { ctx ->
            getReportVersion(name, version, ctx)
            ctx.dsl.select(FILE_INPUT.FILENAME, FILE.SIZE)
                    .from(FILE_INPUT)
                    .innerJoin(FILE)
                    .on(FILE_INPUT.FILE_HASH.eq(FILE.HASH))
                    .where(FILE_INPUT.REPORT_VERSION.eq(version))
                    .and(FILE_INPUT.FILE_PURPOSE.eq(FilePurpose.RESOURCE.toString()))
                    .fetch()
                    .map { FileInfo(it[FILE_INPUT.FILENAME], it[FILE.SIZE]) }

        }
    }

    private fun getReportVersion(name: String, version: String, ctx: JooqContext): ReportVersionRecord
    {
        //raise exception if version does not belong to named report, or version does not exist
        return ctx.dsl.selectFrom(REPORT_VERSION)
                .where(REPORT_VERSION.REPORT.eq(name))
                .and(REPORT_VERSION.ID.eq(version))
                .and(shouldIncludeReportVersion)
                .singleOrNull()
                ?: throw UnknownObjectError("$name-$version", "reportVersion")
    }

    private fun getDatedChangelogForReport(report: String, latestDate: Timestamp, ctx: JooqContext): List<Changelog>
    {
        return ctx.dsl
                .select(changelogReportVersionColumnForUser.`as`("REPORT_VERSION"),
                        CHANGELOG.LABEL,
                        CHANGELOG.VALUE,
                        CHANGELOG.FROM_FILE,
                        CHANGELOG_LABEL.PUBLIC)
                .fromJoinPath(CHANGELOG, CHANGELOG_LABEL)
                .join(REPORT_VERSION)
                .on(changelogReportVersionColumnForUser.eq(REPORT_VERSION.ID))
                .where(REPORT_VERSION.REPORT.eq(report))
                .and(REPORT_VERSION.DATE.lessOrEqual(latestDate))
                .and(shouldIncludeChangelogItem)
                .orderBy(CHANGELOG.ORDERING.desc())
                .fetchInto(Changelog::class.java)
    }

    // shouldInclude for the relational schema
    private val shouldIncludeReportVersion =
            (REPORT_VERSION.REPORT.`in`(reportReadingScopes).or(isGlobalReader.or(isReviewer)))
                    .and(REPORT_VERSION.PUBLISHED.bitOr(isReviewer))

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
