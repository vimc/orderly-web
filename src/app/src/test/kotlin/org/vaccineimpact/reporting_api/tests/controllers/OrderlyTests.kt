package org.vaccineimpact.reporting_api.tests.controllers

import org.assertj.core.api.Assertions.assertThat
import org.json.JSONObject
import org.junit.Test
import org.vaccineimpact.reporting_api.Orderly
import org.vaccineimpact.reporting_api.db.Config
import org.vaccineimpact.reporting_api.test_helpers.DatabaseTest
import org.vaccineimpact.reporting_api.test_helpers.insertReport
import java.io.File

class OrderlyTests : DatabaseTest() {

    private fun createSut(): Orderly {
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

    @Test
    fun `can get artefacts for report`() {

        val artefactString = "{\"mygraph.png\":{\"format\":\"staticgraph\",\"description\":\"A plot of coverage over time\"}}"
        insertReport("test", "version1", artefacts = artefactString)

        val sut = createSut()

        val result = sut.getArtefacts("test", "version1")

        assertThat(result.toString()).isEqualTo(artefactString)
    }

}