package org.vaccineimpact.reporting_api

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.jooq.TableField
import org.vaccineimpact.reporting_api.db.JooqContext
import org.vaccineimpact.reporting_api.db.Tables.*
import org.vaccineimpact.reporting_api.db.tables.records.OrderlyRecord

class Orderly : OrderlyClient {

    val gsonParser = JsonParser()

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

            return it.dsl.select(ORDERLY.ID)
                    .from(ORDERLY)
                    .where(ORDERLY.NAME.eq(name))
                    .fetchInto(String::class.java)
        }

    }

    override fun getReportsByNameAndVersion(name: String, version: String): JsonObject {
        JooqContext().use {

            var test = it.dsl.select()
                    .from(ORDERLY)
                    .where(ORDERLY.NAME.eq(name).and((ORDERLY.ID).eq(version)))
                    .fetchAny()

            var obj = JsonObject()

            for (field in test.fields()){

                var value = test.get(field.name)
                var valAsJson =
                        Serializer.instance.gson.toJson(value)
                var key = field.name

                obj.add(key, JsonParser().parse(valAsJson))
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
            return gsonParser.parse(it.dsl.select(column)
                    .from(ORDERLY)
                    .where(ORDERLY.NAME.eq(name).and((ORDERLY.ID).eq(version)))
                    .fetchAnyInto(String::class.java))
                    .asJsonObject
        }
    }

}
