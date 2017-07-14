package org.vaccineimpact.reporting_api.tests.database_tests

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.reporting_api.db.Orderly
import org.vaccineimpact.reporting_api.test_helpers.DatabaseTests
import org.vaccineimpact.reporting_api.test_helpers.insertReport

class DataTests: DatabaseTests()
{

    private fun createSut(): Orderly {
        return Orderly()
    }

    @Test
    fun `returns true if report has data`() {

        val dataHashString = "{\"dat\":\"07dffb00305279935544238b39d7b14b\"}"
        insertReport("test", "version1", hashData = dataHashString)

        val sut = createSut()

        val result = sut.hasData("test", "version1", "dat")

        assertThat(result).isTrue()
    }

    @Test
    fun `returns false if report does not have data`() {

        val dataHashString = "{\"dat2\":\"07dffb00305279935544238b39d7b14b\"}"

        insertReport("test", "version1", hashData = dataHashString)

        val sut = createSut()

        val result = sut.hasData("test", "version1", "dat")

        assertThat(result).isFalse()
    }

    @Test
    fun `can get data hash for report`() {

        val dataHashString = "{\"dat\":\"07dffb00305279935544238b39d7b14b\"}"

        insertReport("test", "version1", hashData = dataHashString)

        val sut = createSut()

        val result = sut.getData("test", "version1")

        assertThat(result["dat"].asString).isEqualTo("07dffb00305279935544238b39d7b14b")
    }

}
