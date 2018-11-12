package org.vaccineimpact.reporting_api.db

import com.google.gson.*
import org.jooq.TableField
import org.jooq.impl.DSL.*
import org.vaccineimpact.api.models.Changelog
import org.vaccineimpact.api.models.Report
import org.vaccineimpact.api.models.ReportVersion
import org.vaccineimpact.reporting_api.db.Tables.CHANGELOG
import org.vaccineimpact.reporting_api.db.Tables.ORDERLY
import org.vaccineimpact.reporting_api.db.Tables.REPORT
import org.vaccineimpact.reporting_api.db.Tables.REPORT_VERSION
import org.vaccineimpact.reporting_api.db.tables.records.OrderlyRecord
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
                            REPORT_VERSION.DATE.`as`("updatedOn"),
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
                            ORDERLY.ID.`as`("latestVersion"),
                            ORDERLY.PUBLISHED,
                            ORDERLY.DATE.`as`("updatedOn"),
                            ORDERLY.AUTHOR,
                            ORDERLY.REQUESTER)
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

    override fun getReportsByNameAndVersion(name: String, version: String): JsonObject
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

    override fun getChangelogByNameAndVersion(name: String, version: String): List<Changelog>
    {
        JooqContext().use {

            //raise exception if version does not belong to named report, or version does not exist
            val thisVersion =
                    it.dsl.selectFrom(REPORT_VERSION)
                    .where(REPORT_VERSION.REPORT.eq(name))
                    .and(REPORT_VERSION.ID.eq(version))
                    .singleOrNull()
                    ?: throw UnknownObjectError("$name-$version", "reportVersion")


            return it.dsl
                    .select(CHANGELOG.REPORT_VERSION,
                            CHANGELOG.LABEL,
                            CHANGELOG.VALUE,
                            CHANGELOG.FROM_FILE)
                    .from(REPORT_VERSION)
                    .join(CHANGELOG)
                    .on(CHANGELOG.REPORT_VERSION.eq(REPORT_VERSION.ID))
                    .where(REPORT_VERSION.REPORT.eq(thisVersion.report))
                    .and(REPORT_VERSION.DATE.lessOrEqual(thisVersion.date))
                    .orderBy(CHANGELOG.ID.desc())
                    .fetchInto(Changelog::class.java)

        }

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
