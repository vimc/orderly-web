package org.vaccineimpact.reporting_api.tests.database_tests

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.api.models.Scope
import org.vaccineimpact.api.models.permissions.ReifiedPermission
import org.vaccineimpact.reporting_api.ActionContext
import org.vaccineimpact.reporting_api.db.Orderly
import org.vaccineimpact.reporting_api.tests.insertReport

class OrderlyAdminTests : DatabaseTests()
{

    private fun createSut(): Orderly
    {
        val actionContext = mock<ActionContext> {
            on { this.hasPermission(ReifiedPermission("reports.review", Scope.Global())) } doReturn true
        }

        return Orderly(actionContext)
    }

    @Test
    fun `can get all published and unpublished report names`()
    {

        insertReport("test", "version1")
        insertReport("test", "version2")
        insertReport("test2", "test2version1")
        insertReport("test3", "test3version", published = false)

        val sut = createSut()

        val results = sut.getAllReports()

        assertThat(results.count()).isEqualTo(3)
        assertThat(results[0]).isEqualTo("test")
        assertThat(results[1]).isEqualTo("test2")
        assertThat(results[2]).isEqualTo("test3")
    }

    @Test
    fun `can get unpublished report metadata`()
    {

        insertReport("test", "version1",
                hashArtefacts = "{\"summary.csv\":\"07dffb00305279935544238b39d7b14b\"}", published = false)

        val sut = createSut()

        val result = sut.getReportsByNameAndVersion("test", "version1")

        assertThat(result.has("name")).isTrue()
        assertThat(result.has("id")).isTrue()

        assertThat(result.has("hash_artefacts")).isTrue()
        assertThat(result["hash_artefacts"].asJsonObject.has("summary.csv")).isTrue()
    }


    @Test
    fun `can get all published and unpublished report versions`()
    {

        insertReport("test", "version1")
        insertReport("test", "version2")
        insertReport("test", "version3", published = false)

        val sut = createSut()

        val results = sut.getReportsByName("test")

        assertThat(results.count()).isEqualTo(3)
        assertThat(results[0]).isEqualTo("version1")
        assertThat(results[1]).isEqualTo("version2")
        assertThat(results[2]).isEqualTo("version3")

    }


}