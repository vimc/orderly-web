package org.vaccineimpact.reporting_api.tests.database_tests

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.vaccineimpact.reporting_api.ActionContext
import org.vaccineimpact.reporting_api.db.Orderly
import org.vaccineimpact.reporting_api.errors.UnknownObjectError
import org.vaccineimpact.reporting_api.tests.insertReport

class DataTests : DatabaseTests()
{

    private fun createSut(): Orderly
    {
        val actionContext = mock<ActionContext> {
            on { this.hasPermission(org.vaccineimpact.api.models.permissions.ReifiedPermission("reports.read", org.vaccineimpact.api.models.Scope.Global())) } doReturn false
        }

        return Orderly(actionContext)
    }

    @Test
    fun `return datum hash name if report has data`()
    {

        val hash = "07dffb00305279935544238b39d7b14b"
        val dataHashString = "{\"data.csv\":\"$hash\"}"
        insertReport("test", "version1", hashData = dataHashString)

        val sut = createSut()

        val result = sut.getDatum("test", "version1", "data.csv")

        assertThat(result).isEqualTo(hash)
    }

    @Test
    fun `throw unknown object error if report does not have data`()
    {

        val dataHashString = "{\"data2.rds\":\"07dffb00305279935544238b39d7b14b\"}"

        insertReport("test", "version1", hashData = dataHashString)

        val sut = createSut()

        assertThatThrownBy { sut.getDatum("test", "version1", "data.csv") }
                .isInstanceOf(UnknownObjectError::class.java)
    }

    @Test
    fun `can get data hash for report`()
    {

        val dataHashString = "{\"data.csv\":\"07dffb00305279935544238b39d7b14b\"}"

        insertReport("test", "version1", hashData = dataHashString)

        val sut = createSut()

        val result = sut.getData("test", "version1")

        assertThat(result["data.csv"].asString).isEqualTo("07dffb00305279935544238b39d7b14b")
    }

}
