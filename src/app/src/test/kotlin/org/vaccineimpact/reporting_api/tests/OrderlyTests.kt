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

        insertReport("test", "version1")

        val sut = createSut()

        val result = sut.getReportsByNameAndVersion("test", "version1")

    }


    @Test
    fun `can get all reports versions`() {

        insertReport("test", "version1")
        insertReport("test", "version2")

//        val sut = createSut()
//
//        val results = sut.getReportsByName("test")
//
//        assertThat(results.count()).isEqualTo(2)
//        assertThat(results[0]).isEqualTo("version1")
//        assertThat(results[1]).isEqualTo("version2")

        JooqContext().use {

            var test = it.dsl.select()
                    .from(Tables.ORDERLY)
                    .where(Tables.ORDERLY.NAME.eq("test").and((Tables.ORDERLY.ID).eq("version1")))
                    .fetchAny()

            var obj = JsonObject()


            for (field in test.fields()){

                var value = test.get(field.name)
                var valAsJson =
                        Serializer.instance.gson.toJson(value)
                var key = field.name

                obj.add(key, JsonParser().parse(valAsJson))
            }

            var hello = 2
        }
    }



}