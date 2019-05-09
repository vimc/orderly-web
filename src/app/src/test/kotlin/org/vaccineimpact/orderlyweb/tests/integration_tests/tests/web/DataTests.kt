package org.vaccineimpact.orderlyweb.tests.integration_tests.tests.web

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.Tables
import org.vaccineimpact.orderlyweb.tests.integration_tests.helpers.fakeGlobalReportReader
import org.vaccineimpact.orderlyweb.tests.integration_tests.helpers.fakeGlobalReportReviewer
import org.vaccineimpact.orderlyweb.tests.integration_tests.tests.IntegrationTest

class DataTests : IntegrationTest()
{
    @Test
    fun `report readers can get csv data`()
    {
        val dataHash = getAnyDataHash()

        val response = requestHelper.post("/data/csv/$dataHash/", mapOf(),
                userEmail = fakeGlobalReportReader())

        assertThat(response.statusCode).isEqualTo(200)
    }

    @Test
    fun `non report readers cannot get csv data`()
    {
        val dataHash = getAnyDataHash()

        val response = requestHelper.post("/data/csv/$dataHash/", mapOf(),
                userEmail = "no@permissions.com")

        assertThat(response.statusCode).isEqualTo(403)
    }

    @Test
    fun `report readers can get rds data`()
    {
        val dataHash = getAnyDataHash()

        val response = requestHelper.post("/data/rds/$dataHash/", mapOf(),
                userEmail = fakeGlobalReportReader())

        assertThat(response.statusCode).isEqualTo(200)
    }

    @Test
    fun `non report readers cannot get rds data`()
    {
        val dataHash = getAnyDataHash()

        val response = requestHelper.post("/data/rds/$dataHash/", mapOf(),
                userEmail = "no@permissions.com")

        assertThat(response.statusCode).isEqualTo(403)
    }

    private fun getAnyDataHash() : String
    {
        return JooqContext("git/orderly.sqlite").use {

            it.dsl.select(Tables.DATA.HASH)
                    .from(Tables.DATA)
                    .fetchAny()[Tables.DATA.HASH]
        }
    }
}