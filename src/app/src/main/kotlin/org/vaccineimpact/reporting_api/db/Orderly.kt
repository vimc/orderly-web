package org.vaccineimpact.reporting_api.db

import com.github.salomonbrys.kotson.addPropertyIfNotNull
import com.google.gson.*
import org.jooq.TableField
import org.jooq.impl.DSL.*
import org.vaccineimpact.api.models.Changelog
import org.vaccineimpact.api.models.Report
import org.vaccineimpact.api.models.ReportVersion
import org.vaccineimpact.api.models.ReportVersionDetails
import org.vaccineimpact.reporting_api.db.Tables.CHANGELOG
import org.vaccineimpact.reporting_api.db.Tables.CHANGELOG_LABEL
import org.vaccineimpact.reporting_api.db.Tables.FILE_INPUT
import org.vaccineimpact.reporting_api.db.Tables.ORDERLY
import org.vaccineimpact.reporting_api.db.Tables.REPORT
import org.vaccineimpact.reporting_api.db.Tables.REPORT_VERSION
import org.vaccineimpact.reporting_api.db.tables.records.OrderlyRecord
import org.vaccineimpact.reporting_api.db.tables.records.ReportVersionRecord
import org.vaccineimpact.reporting_api.errors.UnknownObjectError
import java.sql.Timestamp

class Orderly(isReviewer: Boolean = false) : OrderlyClient
{
    override fun getAllReportVersions(): List<ReportVersion>
    {
        JooqContext().use {

            // create a temp table containing the latest version ID for each report name
            val latestVersionForEachReport = it.dsl.select(
                    REPORT_VERSION.REPORT,
                    REPORT_VERSION.ID.`as`("latestVersion"),
                    REPORT_VERSION.DATE.max().`as`("maxDate")
            )
                    .from(REPORT_VERSION)
                    .where(shouldIncludeReportVersion)
                    .groupBy(REPORT_VERSION.REPORT)
                    .asTemporaryTable(name = "latest_version_for_each_report")

            //Use relational schema
            return it.dsl.withTemporaryTable(latestVersionForEachReport)
                    .select(REPORT_VERSION.REPORT.`as`("name"),
                            REPORT_VERSION.DISPLAYNAME,
                            REPORT_VERSION.ID,
                            REPORT_VERSION.PUBLISHED,
                            REPORT_VERSION.DATE,
                            REPORT_VERSION.AUTHOR,
                            REPORT_VERSION.REQUESTER,
                            latestVersionForEachReport.field<String>("latestVersion")
                            )
                    .from(REPORT_VERSION)
                    .join(latestVersionForEachReport.tableName)
                    .on(REPORT_VERSION.REPORT.eq(latestVersionForEachReport.field("report")))
                    .where(shouldIncludeReportVersion)
                    .orderBy(REPORT_VERSION.REPORT, REPORT_VERSION.ID)
                    .fetchInto(ReportVersion::class.java)
        }
    }

    private val gsonParser = JsonParser()

    private val shouldInclude = ORDERLY.PUBLISHED.bitOr(isReviewer)

    // shouldInclude for the relational schema
    private val shouldIncludeReportVersion = REPORT_VERSION.PUBLISHED.bitOr(isReviewer)

    private val shouldIncludeChangelogItem =
            if (isReviewer)
                trueCondition()
            else
                CHANGELOG.LABEL.`in`(
                        select(CHANGELOG_LABEL.ID)
                                .from(CHANGELOG_LABEL)
                                .where(CHANGELOG_LABEL.PUBLIC)
                )

    override fun getAllReports(): List<Report>
    {
        JooqContext().use {

            val tempTable = "all"

            val allReports = it.dsl.select(ORDERLY.NAME,
                    ORDERLY.DATE.max().`as`("maxDate"))
                    .from(ORDERLY)
                    .where(shouldInclude)
                    .groupBy(ORDERLY.NAME)

            return it.dsl.with(tempTable).`as`(allReports)
                    .select(ORDERLY.NAME, ORDERLY.DISPLAYNAME,
                            ORDERLY.ID.`as`("latestVersion"))
                    .from(ORDERLY)
                    .join(table(name(tempTable)))
                    .on(ORDERLY.NAME.eq(field(name(tempTable, "name"), String::class.java))
                            .and(ORDERLY.DATE.eq(field(name(tempTable, "maxDate"), Timestamp::class.java))))
                    .where(shouldInclude)
                    .fetchInto(Report::class.java)
        }

    }

    override fun getReportsByName(name: String): List<String>
    {
        JooqContext().use {

            val result = it.dsl.select(ORDERLY.ID)
                    .from(ORDERLY)
                    .where(ORDERLY.NAME.eq(name)
                            .and(shouldInclude))

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

    override fun getReportByNameAndVersion(name: String, version: String): JsonObject
    {
        JooqContext().use {

           val result = it.dsl.select()
                    .from(ORDERLY)
                    .where(ORDERLY.NAME.eq(name)
                            .and((ORDERLY.ID).eq(version))
                            .and(shouldInclude))
                    .fetchAny() ?: throw UnknownObjectError("$name-$version", "reportVersion")

            val obj = JsonObject()

            for (field in result.fields())
            {

                val value = result.get(field)

                val valAsJson = if (value != null)
                {
                    val valueString = value.toString()

                    try
                    {
                        gsonParser.parse(valueString)
                    }
                    catch (e: JsonParseException)
                    {
                        JsonPrimitive(valueString)
                    }
                }
                else
                {
                    JsonNull.INSTANCE
                }

                obj.add(field.name, valAsJson)

            }

            return obj
        }

    }

    ///This is a temporary location for logic accessing the new Orderly schema, until it is completed and replaces
    ///GetReportByNameAndVersion above
    override fun getDetailsByNameAndVersion(name: String, version: String): ReportVersionDetails
    {
        JooqContext().use {

            val reportVersionResult = getReportVersion(name, version, it)

            //get script
            val scriptResult = it.dsl.selectFrom(FILE_INPUT)
                    .where(FILE_INPUT.REPORT_VERSION.eq(version))
                    .and(FILE_INPUT.FILE_PURPOSE.eq("script"))
                    .singleOrNull()


            val result = ReportVersionDetails( id = reportVersionResult.id,
                    name = reportVersionResult.report,
                    displayName = reportVersionResult.displayname,
                    author = reportVersionResult.author,
                    comment = reportVersionResult.comment,
                    date = reportVersionResult.date.toInstant(),
                    description = reportVersionResult.description,
                    published = reportVersionResult.published,
                    requester = reportVersionResult.requester,
                    script = scriptResult?.filename,
                    hashScript = scriptResult?.fileHash)

            return result

        }

    }

    override fun getArtefacts(name: String, version: String): JsonObject
    {
        return getSimpleMap(name, version, ORDERLY.HASH_ARTEFACTS)
    }

    override fun getArtefact(name: String, version: String, filename: String): String
    {
        val result = getSimpleMap(name, version, ORDERLY.HASH_ARTEFACTS)[filename]
                ?: throw UnknownObjectError(filename, "Artefact")

        return result.asString
    }

    override fun getData(name: String, version: String): JsonObject
    {
        return getSimpleMap(name, version, ORDERLY.HASH_DATA)
    }

    override fun getDatum(name: String, version: String, datumname: String): String
    {
        val result = getSimpleMap(name, version, ORDERLY.HASH_DATA)[datumname]
                ?: throw UnknownObjectError(datumname, "Data")

        return result.asString
    }

    override fun getResources(name: String, version: String): JsonObject
    {
        return getSimpleMap(name, version, ORDERLY.HASH_RESOURCES)
    }

    override fun getResource(name: String, version: String, resourcename: String): String
    {
        val result = getSimpleMap(name, version, ORDERLY.HASH_RESOURCES)[resourcename]
                ?: throw UnknownObjectError(resourcename, "Resource")

        return result.asString
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

    private fun getDatedChangelogForReport(report: String, latestDate: Timestamp, ctx: JooqContext) : List<Changelog>
    {
        return ctx.dsl
                .select(CHANGELOG.REPORT_VERSION,
                        CHANGELOG.LABEL,
                        CHANGELOG.VALUE,
                        CHANGELOG.FROM_FILE)
                .from(REPORT_VERSION)
                .join(CHANGELOG)
                .on(CHANGELOG.REPORT_VERSION.eq(REPORT_VERSION.ID))
                .where(REPORT_VERSION.REPORT.eq(report))
                .and(REPORT_VERSION.DATE.lessOrEqual(latestDate))
                .and(shouldIncludeChangelogItem)
                .orderBy(CHANGELOG.ID.desc())
                .fetchInto(Changelog::class.java)
    }


    private fun getSimpleMap(name: String, version: String, column: TableField<OrderlyRecord, String>): JsonObject
    {
        JooqContext().use {
            val result = it.dsl.select(column)
                    .from(ORDERLY)
                    .where(ORDERLY.NAME.eq(name).and((ORDERLY.ID).eq(version))
                            .and(shouldInclude))
                    .fetchAny() ?: throw UnknownObjectError("$name-$version", "reportVersion")

            if (result.value1() == null)
                return JsonObject()

            return gsonParser.parse(result
                    .into(String::class.java))
                    .asJsonObject
        }
    }

}
