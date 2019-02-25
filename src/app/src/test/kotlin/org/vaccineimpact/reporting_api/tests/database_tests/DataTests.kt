package org.vaccineimpact.reporting_api.tests.database_tests

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.vaccineimpact.reporting_api.db.Orderly
import org.vaccineimpact.reporting_api.errors.UnknownObjectError
import org.vaccineimpact.reporting_api.tests.insertData
import org.vaccineimpact.reporting_api.tests.insertReport

class DataTests : CleanDatabaseTests()
{

    private fun createSut(): Orderly
    {
        return Orderly(false)
    }

    @Test
    fun `return datum hash name if report has data`()
    {

        val hash = "07dffb00305279935544238b39d7b14b"
        insertReport("test", "version1")
        insertData("version1", "data.csv", "SELECT * FROM THING", hash)

        val sut = createSut()

        val result = sut.getDatum("test", "version1", "data.csv")

        assertThat(result).isEqualTo(hash)
    }

    @Test
    fun `throw unknown object error if report does not have data`()
    {
        insertReport("test", "version1")

        val hash = "07dffb00305279935544238b39d7b14b"
        insertReport("test", "version1")
        insertData("version1", "data2.rds", "SELECT * FROM THING", hash)

        val sut = createSut()

        assertThatThrownBy { sut.getDatum("test", "version1", "data.csv") }
                .isInstanceOf(UnknownObjectError::class.java)
    }

    @Test
    fun `can get data hash for report`()
    {
        insertReport("test", "version1")
        insertData("version1", "data.csv", "SELECT * FROM THING", "07dffb00305279935544238b39d7b14b")

        val sut = createSut()

        val result = sut.getData("test", "version1")

        assertThat(result["data.csv"]).isEqualTo("07dffb00305279935544238b39d7b14b")
    }

}
