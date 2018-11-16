package org.vaccineimpact.reporting_api.tests.database_tests

import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.reporting_api.db.Orderly
import org.vaccineimpact.reporting_api.errors.UnknownObjectError
import org.vaccineimpact.reporting_api.tests.insertReport

class ArtefactTests : CleanDatabaseTests()
{

    private fun createSut(): Orderly
    {
        return Orderly(false)
    }

    @Test
    fun `returns artefact if report has artefact`()
    {

        val artefactHashString = "{\"summary.csv\":\"07dffb00305279935544238b39d7b14b\"," +
                "\"graph.png\":\"4b89e0b767cee1c30f2e910684189680\"}"

        insertReport("test", "version1", hashArtefacts = artefactHashString)

        val sut = createSut()

        val result = sut.getArtefact("test", "version1", "summary.csv")

        assertThat(result).isNotNull()
    }

    @Test
    fun `throws unknown object error if report does not have artefact`()
    {

        val artefactHashString = "{\"summary.csv\":\"07dffb00305279935544238b39d7b14b\"," +
                "\"graph.png\":\"4b89e0b767cee1c30f2e910684189680\"}"

        insertReport("test", "version1", hashArtefacts = artefactHashString)

        val sut = createSut()

        Assertions.assertThatThrownBy { sut.getArtefact("test", "version1", "details.csv") }
                .isInstanceOf(UnknownObjectError::class.java)

    }

    @Test
    fun `throws unknown object error if report not published`()
    {

        val artefactHashString = "{\"summary.csv\":\"07dffb00305279935544238b39d7b14b\"," +
                "\"graph.png\":\"4b89e0b767cee1c30f2e910684189680\"}"

        insertReport("test", "version1", hashArtefacts = artefactHashString, published = false)

        val sut = createSut()

        Assertions.assertThatThrownBy { sut.getArtefact("test", "version1", "graph.png") }
                .isInstanceOf(UnknownObjectError::class.java)

    }

    @Test
    fun `can get artefacts hash for report`()
    {

        val artefactHashString = "{\"summary.csv\":\"07dffb00305279935544238b39d7b14b\"," +
                "\"graph.png\":\"4b89e0b767cee1c30f2e910684189680\"}"

        insertReport("test", "version1", hashArtefacts = artefactHashString)

        val sut = createSut()

        val result = sut.getArtefacts("test", "version1")

        assertThat(result["summary.csv"].asString).isEqualTo("07dffb00305279935544238b39d7b14b")
        assertThat(result["graph.png"].asString).isEqualTo("4b89e0b767cee1c30f2e910684189680")
    }

}
