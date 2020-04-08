package org.vaccineimpact.orderlyweb.tests.database_tests.ReportRepositoryTests

import org.assertj.core.api.Assertions
import org.junit.Test
import org.vaccineimpact.orderlyweb.db.repositories.OrderlyReportRepository
import org.vaccineimpact.orderlyweb.db.repositories.ReportRepository
import org.vaccineimpact.orderlyweb.test_helpers.CleanDatabaseTests
import org.vaccineimpact.orderlyweb.test_helpers.insertReportWithCustomFields

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
        Assertions.assertThat(result.keys).containsExactly("author", "requester")
        Assertions.assertThat(result["author"]).isEqualTo(null)
        Assertions.assertThat(result["requester"]).isEqualTo(null)
    }

    @Test
    fun `can get custom fields for versions`()
    {
        insertReportWithCustomFields("test1", "v1", mapOf("author" to "authorer"))
        insertReportWithCustomFields("test2", "v2", mapOf())
        insertReportWithCustomFields("test2", "v3", mapOf("requester" to "requester mcfunderface"))

        val sut = createSut()
        val result = sut.getCustomFieldsForVersions(listOf("v1", "v2", "v3"))

        Assertions.assertThat(result.keys).containsExactly("v1", "v3")
        Assertions.assertThat(result["v1"]!!.keys).containsExactly("author")
        Assertions.assertThat(result["v1"]!!["author"]).isEqualTo("authorer")
        Assertions.assertThat(result["v3"]!!.keys).containsExactly("requester")
        Assertions.assertThat(result["v3"]!!["requester"]).isEqualTo("requester mcfunderface")
    }
}
