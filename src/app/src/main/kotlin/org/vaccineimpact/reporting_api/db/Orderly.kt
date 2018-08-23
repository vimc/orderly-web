package org.vaccineimpact.reporting_api.db

import com.google.gson.*
import org.jooq.TableField
import org.jooq.impl.DSL.*
import org.vaccineimpact.api.models.Report
import org.vaccineimpact.api.models.ReportVersion
import org.vaccineimpact.reporting_api.db.Tables.ORDERLY
import org.vaccineimpact.reporting_api.db.tables.records.OrderlyRecord
import org.vaccineimpact.reporting_api.errors.UnknownObjectError
import java.sql.Timestamp

class Orderly(isReviewer: Boolean = false) : OrderlyClient
{
    companion object
    {
        val tempLatestVersions = "latest"
        val tempAll = "all"

        val LATEST_VERSIONS = table(name(tempLatestVersions))
        val VERSION_ID = field(name(tempLatestVersions, "latestVersion"), String::class.java)

        val ALL_VERSIONS = table(name(tempAll))
        val LATEST_VERSION_ID = field(name(tempAll, "latestVersion"), String::class.java)
        val NAME = field(name(tempAll, "name"), String::class.java)

    }

    override fun getAllReportVersions(): List<ReportVersion>
    {
        // Sorry for this hard to follow SQL
        JooqContext().use {

            // this temp table contains all the latest version ids
            val latestVersions = it.dsl.select(ORDERLY.ID.`as`("latestVersion"),
                    ORDERLY.DATE.max().`as`("maxDate"))
                    .from(ORDERLY)
                    .where(shouldInclude)
                    .groupBy(ORDERLY.NAME)

            // this temp table, built using the previous one, contains all report names along
            // with the latest version id for that name
            val allNamesAndLatestVersionIds = it.dsl.with(tempLatestVersions).`as`(latestVersions)
                    .select(ORDERLY.NAME,
                            VERSION_ID)
                    .from(ORDERLY)
                    .join(LATEST_VERSIONS)
                    .on(ORDERLY.ID.eq(VERSION_ID))

            // finally we can join the names and latest version table to the whole table
            // to get all the details we need including the id of the latest version
            return it.dsl.with(tempAll).`as`(allNamesAndLatestVersionIds)
                    .select(ORDERLY.NAME,
                            ORDERLY.DISPLAYNAME,
                            ORDERLY.ID,
                            ORDERLY.PUBLISHED,
                            ORDERLY.DATE.`as`("updatedOn"),
                            ORDERLY.AUTHOR,
                            ORDERLY.REQUESTER,
                            LATEST_VERSION_ID)
                    .from(ORDERLY)
                    .join(ALL_VERSIONS)
                    .on(ORDERLY.NAME.eq(NAME))
                    .where(shouldInclude)
                    .fetchInto(ReportVersion::class.java)
        }
    }

    private val gsonParser = JsonParser()

    private val shouldInclude = ORDERLY.PUBLISHED.bitOr(isReviewer)

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
