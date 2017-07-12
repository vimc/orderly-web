package org.vaccineimpact.reporting_api.tests.db

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.reporting_api.ArtefactType
import org.vaccineimpact.reporting_api.Orderly
import org.vaccineimpact.reporting_api.test_helpers.DatabaseTests
import org.vaccineimpact.reporting_api.test_helpers.insertReport

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

    @Test
    fun `can get artefacts for report`() {

        val artefactString = "[{\"data\":{\"description\":\"A summary table\"," +
                "\"filename\":[\"summary.csv\"]}},{\"staticgraph\"" +
                ":{\"description\":\"A summary graph\",\"filename\":[\"graph.png\"]}}]"

        insertReport("test", "version1", artefacts = artefactString)

        val sut = createSut()

        val result = sut.getArtefacts("test", "version1")

        assertThat(result.count()).isEqualTo(2)
        assertThat(result[0].type).isEqualTo(ArtefactType.DATA)
        assertThat(result[1].type).isEqualTo(ArtefactType.STATICGRAPH)
    }

}