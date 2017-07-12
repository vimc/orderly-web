package org.vaccineimpact.reporting_api

import org.json.JSONObject
import org.vaccineimpact.reporting_api.db.JooqContext
import org.vaccineimpact.reporting_api.db.Tables.*

class Orderly : OrderlyClient
{

    override fun getAllReports(): List<String>
    {
        JooqContext().use {

            return it.dsl.select(ORDERLY.NAME)
                    .from(ORDERLY)
                    .fetchInto(String::class.java)
                    .distinct()
        }

    }

    override fun getReportsByName(name: String): List<String>
    {
        JooqContext().use {

            return it.dsl.select(ORDERLY.ID)
                    .from(ORDERLY)
                    .where(ORDERLY.NAME.eq(name))
                    .fetchInto(String::class.java)
        }

    }


    override fun getReportsByNameAndVersion(name: String, version: String): OrderlyReport
    {
        JooqContext().use {

            return it.dsl.select()
                    .from(ORDERLY)
                    .where(ORDERLY.NAME.eq(name).and((ORDERLY.ID).eq(version)))
                    .fetchAnyInto(OrderlyReport::class.java)
        }

    }

    override fun getArtefacts(name: String, version: String): JSONObject
    {
        JooqContext().use {

            return JSONObject(it.dsl.select(ORDERLY.ARTEFACTS)
                    .from(ORDERLY)
                    .where(ORDERLY.NAME.eq(name).and((ORDERLY.ID).eq(version)))
                    .fetchAnyInto(String::class.java))

        }

    }

    override fun hasArtefact(name: String, version: String, artefact: String): Boolean
    {
        JooqContext().use {

           return JSONObject(it.dsl.select(ORDERLY.ARTEFACTS)
                    .from(ORDERLY)
                    .where(ORDERLY.NAME.eq(name).and((ORDERLY.ID).eq(version)))
                    .fetchAnyInto(String::class.java)).has(artefact)

        }

    }

}
