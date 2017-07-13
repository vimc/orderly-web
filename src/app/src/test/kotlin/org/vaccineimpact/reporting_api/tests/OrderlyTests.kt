package org.vaccineimpact.reporting_api.tests

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.reporting_api.Orderly
import org.vaccineimpact.reporting_api.Serializer
import org.vaccineimpact.reporting_api.test_helpers.DatabaseTests
import org.vaccineimpact.reporting_api.test_helpers.insertReport
import org.vaccineimpact.reporting_api.db.JooqContext
import org.vaccineimpact.reporting_api.db.Tables

class OrderlyTests : DatabaseTests() {

    private fun createSut(): Orderly {
        return Orderly()
    }

    @Test
    fun `can get all report names`() {

        insertReport("test", "version1")
        insertReport("test", "version2")
        insertReport("test2", "test2version1")

        val sut = createSut()

        val results = sut.getAllReports()

        assertThat(results.count()).isEqualTo(2)
        assertThat(results[0]).isEqualTo("test")
        assertThat(results[1]).isEqualTo("test2")
    }

    @Test
    fun `can get report metadata`() {

        insertReport("test", "version1",
                hashArtefacts = "{\"summary.csv\":\"07dffb00305279935544238b39d7b14b\"}")

        val sut = createSut()

        val result = sut.getReportsByNameAndVersion("test", "version1")

        assertThat(result.has("name")).isTrue()
        assertThat(result.has("id")).isTrue()

        assertThat(result.has("hash_artefacts")).isTrue()
        assertThat(result["hash_artefacts"].asJsonObject.has("summary.csv")).isTrue()
    }


    @Test
    fun `can get all reports versions`() {

        insertReport("test", "version1")
        insertReport("test", "version2")

        val sut = createSut()

        val results = sut.getReportsByName("test")

        assertThat(results.count()).isEqualTo(2)
        assertThat(results[0]).isEqualTo("version1")
        assertThat(results[1]).isEqualTo("version2")

    }



}