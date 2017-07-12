package org.vaccineimpact.reporting_api

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

    override fun getReportsByNameAndVersion(name: String, version: String): OrderlyReport {
        JooqContext().use {

            return it.dsl.select()
                    .from(ORDERLY)
                    .where(ORDERLY.NAME.eq(name).and((ORDERLY.ID).eq(version)))
                    .fetchAnyInto(OrderlyReport::class.java)
        }
    }

    override fun getArtefacts(name: String, version: String): Map<String, String> {
        return getSimpleMap(name, version, ORDERLY.HASH_ARTEFACTS)
    }

    override fun hasArtefact(name: String, version: String, filename: String): Boolean {
        return hasKey(name, version, filename, ORDERLY.HASH_ARTEFACTS)
    }

    override fun getData(name: String, version: String): Map<String, String> {
        return getSimpleMap(name, version, ORDERLY.HASH_DATA)
    }

    override fun hasData(name: String, version: String, dataname: String): Boolean {
        return hasKey(name, version, dataname, ORDERLY.HASH_DATA)
    }

    private fun hasKey(name: String, version: String, key: String, column: TableField<OrderlyRecord, String>): Boolean
    {
        JooqContext().use {

            return gsonParser.parse(it.dsl.select(column)
                    .from(ORDERLY)
                    .where(ORDERLY.NAME.eq(name).and((ORDERLY.ID).eq(version)))
                    .fetchAnyInto(String::class.java))
                    .asJsonObject
                    .has(key)
        }
    }

    private fun getSimpleMap(name: String, version: String, column: TableField<OrderlyRecord, String>): Map<String, String>
    {
        JooqContext().use {
            return Json.parseSimpleMap(it.dsl.select(column)
                    .from(ORDERLY)
                    .where(ORDERLY.NAME.eq(name).and((ORDERLY.ID).eq(version)))
                    .fetchAnyInto(String::class.java))
        }
    }

}
