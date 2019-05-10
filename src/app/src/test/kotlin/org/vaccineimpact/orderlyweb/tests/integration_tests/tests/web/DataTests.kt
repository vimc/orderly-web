package org.vaccineimpact.orderlyweb.tests.integration_tests.tests.web

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.orderlyweb.ContentTypes
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.Tables
import org.vaccineimpact.orderlyweb.test_helpers.insertReport
import org.vaccineimpact.orderlyweb.tests.insertData
import org.vaccineimpact.orderlyweb.tests.integration_tests.helpers.fakeGlobalReportReader
import org.vaccineimpact.orderlyweb.tests.integration_tests.tests.IntegrationTest
import java.io.File
import java.net.URLEncoder

class DataTests : IntegrationTest()
{
    @Test
    fun `only report readers can get csv data`()
    {
        val dataHash = getAnyEncodedDataHash()

        assertUrlSecured("/data/csv/$dataHash/", contentType = ContentTypes.csv)
    }


    @Test
    fun `only report readers can get rds data`()
    {
        val dataHash = getAnyEncodedDataHash()

        assertUrlSecured("/data/rds/$dataHash/", contentType = ContentTypes.binarydata)
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