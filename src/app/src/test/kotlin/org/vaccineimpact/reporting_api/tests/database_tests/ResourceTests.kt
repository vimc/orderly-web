package org.vaccineimpact.reporting_api.tests.database_tests

import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.reporting_api.db.Orderly
import org.vaccineimpact.reporting_api.errors.UnknownObjectError
import org.vaccineimpact.reporting_api.tests.insertReport

class ResourceTests : DatabaseTests()
{

    private fun createSut(): Orderly
    {
        return Orderly()
    }

    @Test
    fun `returns resourcename if report has resource`()
    {
        insertReport("test", "version1", hashResources = "{\"resource.csv\": \"gfe7064mvdfjieync\"}")

        val sut = createSut()

        val result = sut.getResource("test", "version1", "[resource.csv]")

        assertThat(result).isNotNull()
    }

    @Test
    fun `throws unknown object error if report does not have artefact`()
    {
        insertReport("test", "version1", hashResources = "{\"resource.csv\": \"gfe7064mvdfjieync\"}")

        val sut = createSut()

        Assertions.assertThatThrownBy { sut.getResource("test", "version1", "details.csv") }
                .isInstanceOf(UnknownObjectError::class.java)

    }

    @Test
    fun `throws unknown object error if report not published`()
    {

        val artefactHashString = "{\"summary.csv\":\"07dffb00305279935544238b39d7b14b\"," +
                "\"graph.png\":\"4b89e0b767cee1c30f2e910684189680\"}"

        insertReport("test", "version1", hashArtefacts = artefactHashString, published = false)

        val sut = createSut()

        Assertions.assertThatThrownBy { sut.getResource("test", "version1", "graph.png") }
                .isInstanceOf(UnknownObjectError::class.java)

    }

}
