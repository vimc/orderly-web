package org.vaccineimpact.reporting_api.db

import com.google.gson.*
import org.jooq.TableField
import org.vaccineimpact.reporting_api.db.Tables.*
import org.vaccineimpact.reporting_api.db.tables.records.OrderlyRecord
import org.vaccineimpact.reporting_api.errors.UnknownObjectError

class Orderly : OrderlyClient {

    private val gsonParser = JsonParser()

    override fun getAllReports(): List<String> {
        JooqContext().use {

            return it.dsl.select(ORDERLY.NAME)
                    .from(ORDERLY)
                    .fetchInto(String::class.java)
                    .distinct()
        }

    }

    override fun getReportsByName(name: String): List<String> {
        JooqContext().use {

            val result = it.dsl.select(ORDERLY.ID)
                    .from(ORDERLY)
                    .where(ORDERLY.NAME.eq(name))

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
                    .where(ORDERLY.NAME.eq(name).and((ORDERLY.ID).eq(version)))
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

    override fun hasArtefact(name: String, version: String, filename: String): Boolean {
        return hasKey(name, version, filename, ORDERLY.HASH_ARTEFACTS)
    }

    override fun getData(name: String, version: String): JsonObject {
        return getSimpleMap(name, version, ORDERLY.HASH_DATA)
    }

    override fun hasData(name: String, version: String, dataname: String): Boolean {
        return hasKey(name, version, dataname, ORDERLY.HASH_DATA)
    }

    override fun getResources(name: String, version: String): JsonObject {
        return getSimpleMap(name, version, ORDERLY.HASH_RESOURCES)
    }

    override fun hasResource(name: String, version: String, resourcename: String): Boolean {
        return hasKey(name, version, resourcename, ORDERLY.HASH_RESOURCES)
    }

    private fun hasKey(name: String, version: String, key: String, column: TableField<OrderlyRecord, String>): Boolean {
        return getSimpleMap(name, version, column)
                .has(key)
    }

    private fun getSimpleMap(name: String, version: String, column: TableField<OrderlyRecord, String>): JsonObject {
        JooqContext().use {
            val result = it.dsl.select(column)
                    .from(ORDERLY)
                    .where(ORDERLY.NAME.eq(name).and((ORDERLY.ID).eq(version)))
                    .fetchAny()?: throw UnknownObjectError("$name-$version", "reportVersion")

            return gsonParser.parse(result
                    .into(String::class.java))
                    .asJsonObject
        }
    }

}
