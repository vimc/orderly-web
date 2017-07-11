package org.vaccineimpact.reporting_api.tests

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.reporting_api.Orderly
import org.vaccineimpact.reporting_api.db.Config
import org.vaccineimpact.reporting_api.test_helpers.DatabaseTest
import org.vaccineimpact.reporting_api.test_helpers.insertReport
import java.io.File

class OrderlyTests : DatabaseTest() {

    private fun createSut(): Orderly{
        return Orderly(File(Config["dbTest.location"]).absolutePath)
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

        assertThat(result.name).isEqualTo("test")
        assertThat(result.id).isEqualTo("version1")
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