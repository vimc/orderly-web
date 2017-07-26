package org.vaccineimpact.reporting_api.db

import com.google.gson.*
import org.jooq.TableField
import org.vaccineimpact.reporting_api.db.Tables.ORDERLY
import org.vaccineimpact.reporting_api.db.tables.records.OrderlyRecord
import org.vaccineimpact.reporting_api.errors.UnknownObjectError

class Orderly : OrderlyClient {

    private val gsonParser = JsonParser()

    override fun getAllReports(): List<String> {
        JooqContext().use {

            return it.dsl.select(ORDERLY.NAME)
                    .from(ORDERLY)
                    .where(ORDERLY.PUBLISHED)
                    .fetchInto(String::class.java)
                    .distinct()
        }

    }

    override fun getReportsByName(name: String): List<String> {
        JooqContext().use {

            val result = it.dsl.select(ORDERLY.ID)
                    .from(ORDERLY)
                    .where(ORDERLY.NAME.eq(name).and(ORDERLY.PUBLISHED))

            if (result.count() == 0) {
                throw UnknownObjectError(name, "report")
            } else {
                return result.fetchInto(String::class.java)
            }
        }
    }

    override fun getReportsByNameAndVersion(name: String, version: String): JsonObject {
        JooqContext().use {

            val result = it.dsl.select()
                    .from(ORDERLY)
                    .where(ORDERLY.NAME.eq(name)
                            .and((ORDERLY.ID).eq(version))
                            .and(ORDERLY.PUBLISHED))
                    .fetchAny() ?: throw UnknownObjectError("$name-$version", "reportVersion")

            val obj = JsonObject()

            for (field in result.fields()) {

                val value = result.get(field)

                val valAsJson = if (value != null) {
                    val valueString = value.toString()

                    try {
                        gsonParser.parse(valueString)
                    } catch(e: JsonParseException) {
                        JsonPrimitive(valueString)
                    }
                } else {
                    JsonNull.INSTANCE
                }

                obj.add(field.name, valAsJson)

            }

            return obj
        }

    }

    override fun getArtefacts(name: String, version: String): JsonObject {
        return getSimpleMap(name, version, ORDERLY.HASH_ARTEFACTS)
    }

    override fun getArtefact(name: String, version: String, filename: String): String {
        val result = getSimpleMap(name, version, ORDERLY.HASH_ARTEFACTS)[filename] ?:
                throw UnknownObjectError(filename, "Artefact")

        return result.asString
    }

    override fun getData(name: String, version: String): JsonObject {
        return getSimpleMap(name, version, ORDERLY.HASH_DATA)
    }

    override fun getDatum(name: String, version: String, datumname: String): String {
        val result = getSimpleMap(name, version, ORDERLY.HASH_DATA)[datumname] ?:
                throw UnknownObjectError(datumname, "Data")

        return result.asString
    }

    override fun getResources(name: String, version: String): JsonObject {
        return getSimpleMap(name, version, ORDERLY.HASH_RESOURCES)
    }

    override fun getResource(name: String, version: String, resourcename: String): String {
        val result = getSimpleMap(name, version, ORDERLY.HASH_RESOURCES)[resourcename] ?:
                throw UnknownObjectError(resourcename, "Resource")

        return result.asString
    }


    private fun getSimpleMap(name: String, version: String, column: TableField<OrderlyRecord, String>): JsonObject {
        JooqContext().use {
            val result = it.dsl.select(column)
                    .from(ORDERLY)
                    .where(ORDERLY.NAME.eq(name).and((ORDERLY.ID).eq(version))
                            .and(ORDERLY.PUBLISHED))
                    .fetchAny() ?: throw UnknownObjectError("$name-$version", "reportVersion")

            if (result.value1() == null)
                return JsonObject()

            return gsonParser.parse(result
                    .into(String::class.java))
                    .asJsonObject
        }
    }

}
