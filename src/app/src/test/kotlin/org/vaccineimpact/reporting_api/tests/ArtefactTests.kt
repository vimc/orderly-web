package org.vaccineimpact.reporting_api.tests

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.reporting_api.db.Orderly
import org.vaccineimpact.reporting_api.test_helpers.DatabaseTests
import org.vaccineimpact.reporting_api.test_helpers.insertReport

class ArtefactTests: DatabaseTests()
{

    private fun createSut(): Orderly {
        return Orderly()
    }

    @Test
    fun `returns true if report has artefact`() {

        val artefactHashString = "{\"summary.csv\":\"07dffb00305279935544238b39d7b14b\"," +
                "\"graph.png\":\"4b89e0b767cee1c30f2e910684189680\"}"

        insertReport("test", "version1", hashArtefacts = artefactHashString)

        val sut = createSut()

        val result = sut.hasArtefact("test", "version1", "summary.csv")

        assertThat(result).isTrue()
    }

    @Test
    fun `returns false if report does not have artefact`() {

        val artefactHashString = "{\"summary.csv\":\"07dffb00305279935544238b39d7b14b\"," +
                "\"graph.png\":\"4b89e0b767cee1c30f2e910684189680\"}"

        insertReport("test", "version1", hashArtefacts = artefactHashString)

        val sut = createSut()

        val result = sut.hasArtefact("test", "version1", "details.csv")

        assertThat(result).isFalse()
    }

    @Test
    fun `can get artefacts hash for report`() {

        val artefactHashString = "{\"summary.csv\":\"07dffb00305279935544238b39d7b14b\"," +
                "\"graph.png\":\"4b89e0b767cee1c30f2e910684189680\"}"

        insertReport("test", "version1", hashArtefacts = artefactHashString)

        val sut = createSut()

        val result = sut.getArtefacts("test", "version1")

        assertThat(result["summary.csv"].asString).isEqualTo("07dffb00305279935544238b39d7b14b")
        assertThat(result["graph.png"].asString).isEqualTo("4b89e0b767cee1c30f2e910684189680")
    }

}
