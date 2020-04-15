package org.vaccineimpact.orderlyweb.tests.database_tests.ReportRepositoryTests

import org.assertj.core.api.Assertions.*
import org.junit.Test
import org.vaccineimpact.orderlyweb.db.repositories.OrderlyReportRepository
import org.vaccineimpact.orderlyweb.db.repositories.ReportRepository
import org.vaccineimpact.orderlyweb.test_helpers.CleanDatabaseTests
import org.vaccineimpact.orderlyweb.test_helpers.insertReport
import org.vaccineimpact.orderlyweb.test_helpers.insertReportWithCustomFields
import org.vaccineimpact.orderlyweb.test_helpers.insertVersionParameterValues

class VersionTests : CleanDatabaseTests() {

    private fun createSut(isReviewer: Boolean = false): ReportRepository
    {
        return OrderlyReportRepository(isReviewer, true, listOf())
    }

    @Test
    fun `can get all custom fields`()
    {
        val sut = createSut()
        val result = sut.getAllCustomFields()
        assertThat(result.keys).containsExactly("author", "requester")
        assertThat(result["author"]).isEqualTo(null)
        assertThat(result["requester"]).isEqualTo(null)
    }

    @Test
    fun `can get custom fields for versions`()
    {
        insertReportWithCustomFields("test1", "v1", mapOf("author" to "authorer"))
        insertReportWithCustomFields("test2", "v2", mapOf())
        insertReportWithCustomFields("test2", "v3", mapOf("requester" to "requester mcfunderface"))

        val sut = createSut()
        val result = sut.getCustomFieldsForVersions(listOf("v1", "v2", "v3"))

        assertThat(result.keys).containsExactly("v1", "v3")
        assertThat(result["v1"]!!.keys).containsExactly("author")
        assertThat(result["v1"]!!["author"]).isEqualTo("authorer")
        assertThat(result["v3"]!!.keys).containsExactly("requester")
        assertThat(result["v3"]!!["requester"]).isEqualTo("requester mcfunderface")
    }

    @Test
    fun `can get parameters for report versions`()
    {
        insertReport("test", "va")
        insertVersionParameterValues("va", mapOf("p1" to "param1", "p2" to "param2"))
        insertReport("test", "vz")
        insertVersionParameterValues("vz", mapOf("p1" to "param3"))

        insertReport("test2", "vc")
        insertReport("test2", "vb")

        val sut = createSut()

        val results = sut.getParametersForVersions(listOf("va", "vz", "vb", "vc"))

        assertThat(results.keys).containsExactly("va", "vz")

        assertThat(results["va"]!!.keys.count()).isEqualTo(2)
        assertThat(results["va"]!!["p1"]).isEqualTo("param1")
        assertThat(results["va"]!!["p2"]).isEqualTo("param2")

        assertThat(results["vz"]!!.keys.count()).isEqualTo(1)
        assertThat(results["vz"]!!["p1"]).isEqualTo("param3")
    }
}
