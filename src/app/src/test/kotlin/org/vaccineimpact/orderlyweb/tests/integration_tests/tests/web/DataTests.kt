package org.vaccineimpact.orderlyweb.tests.integration_tests.tests.web

import org.junit.Test
import org.vaccineimpact.orderlyweb.ContentTypes
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.Tables
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.tests.integration_tests.tests.IntegrationTest
import java.net.URLEncoder

class DataTests : IntegrationTest()
{
    @Test
    fun `only report readers can get csv data`()
    {
        val dataHash = getAnyEncodedDataHash()
        assertWebUrlSecured("/data/csv/$dataHash/", setOf(ReifiedPermission("reports.read", Scope.Global())),
                contentType = ContentTypes.csv)
    }


    @Test
    fun `only report readers can get rds data`()
    {
        val dataHash = getAnyEncodedDataHash()
        assertWebUrlSecured("/data/csv/$dataHash/", setOf(ReifiedPermission("reports.read", Scope.Global())),
                contentType = ContentTypes.binarydata)
    }

    private fun getAnyEncodedDataHash() : String
    {
        val hash =  JooqContext("demo/orderly.sqlite").use {

            it.dsl.select(Tables.DATA.HASH)
                    .from(Tables.DATA)
                    .fetchAny()[Tables.DATA.HASH]
        }

        return URLEncoder.encode(hash, "UTF-8")
    }
}