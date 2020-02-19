package org.vaccineimpact.orderlyweb.db

import org.jooq.Record6
import org.jooq.Result
import org.jooq.impl.DSL.select
import org.jooq.impl.DSL.trueCondition
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.models.*
import org.vaccineimpact.orderlyweb.db.Tables.*
import org.vaccineimpact.orderlyweb.db.tables.records.ReportVersionRecord
import org.vaccineimpact.orderlyweb.errors.UnknownObjectError
import org.vaccineimpact.orderlyweb.models.FileInfo
import java.sql.Timestamp

typealias GenericReportVersionRecord = Record6<String, String, String, Boolean, Timestamp, String>

class Orderly(val isReviewer: Boolean,
              val isGlobalReader: Boolean,
              val reportReadingScopes: List<String> = listOf()) : OrderlyClient
{
    constructor(context: ActionContext): this(context.isReviewer(), context.isGlobalReader(), context.reportReadingScopes)

    override fun getArtefacts(report: String, version: String): List<Artefact>
    {
        JooqContext().use {

            getReportVersion(report, version, it)
            return it.dsl.select(REPORT_VERSION_ARTEFACT.ID, REPORT_VERSION_ARTEFACT.FORMAT,
                    REPORT_VERSION_ARTEFACT.DESCRIPTION)
                    .from(REPORT_VERSION_ARTEFACT)
                    .where(REPORT_VERSION_ARTEFACT.REPORT_VERSION.eq(version))
                    .fetch()
                    .map { a ->
                        val id = a[REPORT_VERSION_ARTEFACT.ID]
                        val format = a[REPORT_VERSION_ARTEFACT.FORMAT]
                        val description = a[REPORT_VERSION_ARTEFACT.DESCRIPTION]
                        val files = it.dsl.select(FILE_ARTEFACT.FILENAME, FILE.SIZE)
                                .from(FILE_ARTEFACT)
                                .innerJoin(FILE)
                                .on(FILE_ARTEFACT.FILE_HASH.eq(FILE.HASH))
                                .where(FILE_ARTEFACT.ARTEFACT.eq(id))
                                .fetch()
                                .map{ r -> FileInfo(r[FILE_ARTEFACT.FILENAME], r[FILE.SIZE]) }

                        Artefact(parseEnum(format), description, files)
                    }
        }
    }

    override fun getAllReportVersions(): List<ReportVersion>
    {
        JooqContext().use {
            // create a temp table containing the latest version ID for each report name
            val latestVersionForEachReport = getLatestVersionsForReports(it)

            val versions =
                    it.dsl.withTemporaryTable(latestVersionForEachReport)
                            .select(REPORT_VERSION.REPORT,
                                    REPORT_VERSION.DISPLAYNAME,
                                    REPORT_VERSION.ID,
                                    REPORT_VERSION.PUBLISHED,
                                    REPORT_VERSION.DATE,
                                    latestVersionForEachReport.field<String>("latestVersion")
                            )
                            .from(REPORT_VERSION)
                            .join(latestVersionForEachReport.tableName)
                            .on(REPORT_VERSION.REPORT.eq(latestVersionForEachReport.field("report")))
                            .where(shouldIncludeReportVersion)
                            .orderBy(REPORT_VERSION.REPORT, REPORT_VERSION.ID)
                            .fetch()

            return mapToReportVersionsWithCustomFields(it, versions)
        }
    }

    override fun getGlobalPinnedReports(): List<ReportVersion>
    {
        JooqContext().use {

            // create a temp table containing the latest visible version ID for each report name
            val latestVersionForEachReport = getLatestVersionsForReports(it)

            val versions =  it.dsl.withTemporaryTable(latestVersionForEachReport)
                    .select(REPORT_VERSION.REPORT,
                            REPORT_VERSION.DISPLAYNAME,
                            REPORT_VERSION.ID,
                            REPORT_VERSION.PUBLISHED,
                            REPORT_VERSION.DATE,
                            latestVersionForEachReport.field<String>("latestVersion")
                    )
                    .from(REPORT_VERSION)
                    .join(latestVersionForEachReport.tableName)
                    .on(REPORT_VERSION.ID.eq(latestVersionForEachReport.field("latestVersion")))
                    .join(ORDERLYWEB_PINNED_REPORT_GLOBAL)
                    .on(ORDERLYWEB_PINNED_REPORT_GLOBAL.REPORT.eq(REPORT_VERSION.REPORT))
                    .orderBy(ORDERLYWEB_PINNED_REPORT_GLOBAL.ORDERING)
                    .fetch()

            return mapToReportVersionsWithCustomFields(it, versions)
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

    override fun getDetailsByNameAndVersion(name: String, version: String): ReportVersionDetails
    {
        JooqContext().use {

            val reportVersionResult = getReportVersion(name, version, it)
            val aretefacts = getArtefacts(name, version)

            return ReportVersionDetails(id = reportVersionResult.id,
                    name = reportVersionResult.report,
                    displayName = reportVersionResult.displayname,
                    date = reportVersionResult.date.toInstant(),
                    description = reportVersionResult.description,
                    published = reportVersionResult.published,
                    artefacts = aretefacts,
                    resources = getResourceFiles(name, version),
                    dataInfo = getDataInfo(name, version))
        }
    }

    override fun getArtefactHashes(name: String, version: String): Map<String, String>
    {
        return JooqContext().use { ctx ->
            getReportVersion(name, version, ctx)
            ctx.dsl.select(FILE_ARTEFACT.FILENAME, FILE_ARTEFACT.FILE_HASH)
                    .from(FILE_ARTEFACT)
                    .join(REPORT_VERSION_ARTEFACT)
                    .on(FILE_ARTEFACT.ARTEFACT.eq(REPORT_VERSION_ARTEFACT.ID))
                    .where(REPORT_VERSION_ARTEFACT.REPORT_VERSION.eq(version))
                    .fetch()
                    .associate { it[FILE_ARTEFACT.FILENAME] to it[FILE_ARTEFACT.FILE_HASH] }
        }
    }

    override fun getArtefactHash(name: String, version: String, filename: String): String
    {
        return getArtefactHashes(name, version)[filename]
                ?: throw UnknownObjectError(filename, "Artefact")
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

    override fun checkVersionExistsForReport(name: String, version: String)
    {
        JooqContext().use {
            getReportVersion(name, version, it)
        }
    }

    private fun mapToReportVersionsWithCustomFields(ctx: JooqContext,
                                                    versions: Result<GenericReportVersionRecord>): List<ReportVersion>
    {
        val allCustomFieldKeys = ctx.dsl.select(
                CUSTOM_FIELDS.ID)
                .from(CUSTOM_FIELDS)
                .fetch()
                .getValues(CUSTOM_FIELDS.ID)

        val versionIds = versions.map{ v -> v[REPORT_VERSION.ID] }
        val customFields = ctx.dsl.select(
                REPORT_VERSION_CUSTOM_FIELDS.KEY,
                REPORT_VERSION_CUSTOM_FIELDS.VALUE,
                REPORT_VERSION_CUSTOM_FIELDS.REPORT_VERSION)
                .from(REPORT_VERSION_CUSTOM_FIELDS)
                .where(REPORT_VERSION_CUSTOM_FIELDS.REPORT_VERSION.`in`(versionIds))
                .fetch()
                .groupBy{ it[REPORT_VERSION_CUSTOM_FIELDS.REPORT_VERSION] }

        return versions.map{
            val id = it[REPORT_VERSION.ID]

            val pairs = allCustomFieldKeys.map{ k -> k to null as String?}.toTypedArray()
            val allCustomFields = mutableMapOf(*pairs)

            val versionCustomFields = customFields[id]!!
                    .associate {f -> f[REPORT_VERSION_CUSTOM_FIELDS.KEY] to f[REPORT_VERSION_CUSTOM_FIELDS.VALUE]}

            allCustomFields.putAll(versionCustomFields)

            ReportVersion(it[REPORT_VERSION.REPORT],
                    it[REPORT_VERSION.DISPLAYNAME],
                    it[REPORT_VERSION.ID],
                    it["latestVersion"] as String,
                    it[REPORT_VERSION.PUBLISHED],
                    it[REPORT_VERSION.DATE].toInstant(),
                    allCustomFields)
        }
    }

    private fun getDataInfo(name: String, version: String): List<DataInfo>
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
                    .map{ r -> DataInfo(r[REPORT_VERSION_DATA.NAME], r[DATA.SIZE_CSV], r[DATA.SIZE_RDS])}
        }
    }

    private fun getResourceFiles(name: String, version: String): List<FileInfo>
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
                    .map { FileInfo( it[FILE_INPUT.FILENAME], it[FILE.SIZE]) }
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

    private fun getDatedChangelogForReport(report: String, latestDate: Timestamp, ctx: JooqContext): List<Changelog>
    {
        return ctx.dsl
                .select(changelogReportVersionColumnForUser.`as`("REPORT_VERSION"),
                        CHANGELOG.LABEL,
                        CHANGELOG.VALUE,
                        CHANGELOG.FROM_FILE)
                .from(CHANGELOG)
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
            (REPORT_VERSION.REPORT.`in`(reportReadingScopes).or(isGlobalReader))
                    .and(REPORT_VERSION.PUBLISHED.bitOr(isReviewer))

    private val shouldIncludeChangelogItem =
            if (isReviewer)
                trueCondition()
            else
                CHANGELOG.LABEL.`in`(
                        select(CHANGELOG_LABEL.ID)
                                .from(CHANGELOG_LABEL)
                                .where(CHANGELOG_LABEL.PUBLIC)
                ).and(CHANGELOG.REPORT_VERSION_PUBLIC.isNotNull)

    private val changelogReportVersionColumnForUser =
            if (isReviewer)
                CHANGELOG.REPORT_VERSION
            else
                CHANGELOG.REPORT_VERSION_PUBLIC

}
