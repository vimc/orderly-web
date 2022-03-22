package org.vaccineimpact.orderlyweb.tests.database_tests

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.vaccineimpact.orderlyweb.db.Orderly
import org.vaccineimpact.orderlyweb.errors.UnknownObjectError
import org.vaccineimpact.orderlyweb.test_helpers.CleanDatabaseTests
import org.vaccineimpact.orderlyweb.test_helpers.insertReport
import org.vaccineimpact.orderlyweb.tests.insertData

class DataTests : CleanDatabaseTests()
{

    private fun createSut(): Orderly
    {
        return Orderly(isReviewer = false, isGlobalReader = true, reportReadingScopes = listOf())
    }

    @Test
    fun `return datum hash name if report has data`()
    {

        val hash = "07dffb00305279935544238b39d7b14b"
        insertReport("test", "version1")
        insertData("version1", "data.csv", "SELECT * FROM THING", "testdb", hash)

        val sut = createSut()

        val result = sut.getDatum("test", "version1", "data.csv")

        assertThat(result).isEqualTo(hash)
    }

    @Test
    fun `throw unknown object error if report does not have data`()
    {
        val hash = "07dffb00305279935544238b39d7b14b"
        insertReport("test", "version1")
        insertData("version1", "data2.rds", "SELECT * FROM THING", "testdb", hash)

        val sut = createSut()

        assertThatThrownBy { sut.getDatum("test", "version1", "data.csv") }
                .isInstanceOf(UnknownObjectError::class.java)
    }

    @Test
    fun `can get data hash for report`()
    {
        insertReport("test", "version1")
        insertData("version1", "data.csv", "SELECT * FROM THING", "testdb", "07dffb00305279935544238b39d7b14b")

        val sut = createSut()

        val result = sut.getData("test", "version1")

        assertThat(result["data.csv"]).isEqualTo("07dffb00305279935544238b39d7b14b")
    }
}
