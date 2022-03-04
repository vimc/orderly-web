package org.vaccineimpact.orderlyweb.tests.database_tests

import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.orderlyweb.db.repositories.ArtefactRepository
import org.vaccineimpact.orderlyweb.db.repositories.OrderlyArtefactRepository
import org.vaccineimpact.orderlyweb.errors.UnknownObjectError
import org.vaccineimpact.orderlyweb.models.FileInfo
import org.vaccineimpact.orderlyweb.test_helpers.CleanDatabaseTests
import org.vaccineimpact.orderlyweb.tests.insertArtefact
import org.vaccineimpact.orderlyweb.test_helpers.insertReport

class ArtefactTests : CleanDatabaseTests()
{
    private val files = listOf(FileInfo("summary.csv", 1234), FileInfo("graph.png", 3456))

    private fun createSut(): ArtefactRepository
    {
        return OrderlyArtefactRepository()
    }

    @Test
    fun `getArtefactHash returns artefact hash if report has artefact`()
    {
        insertReport("test", "version1")
        insertArtefact("version1", files = files)

        val sut = createSut()
        val result = sut.getArtefactHash("test", "version1", "summary.csv")

        assertThat(result).isNotNull()
    }

    @Test
    fun `getArtefactHash throws unknown object error if report does not have artefact`()
    {
        insertReport("test", "version1")
        insertReport("test", "version2")
        insertArtefact("version1", files = files)

        val sut = createSut()

        Assertions.assertThatThrownBy { sut.getArtefactHash("test", "version1", "details.csv") }
                .isInstanceOf(UnknownObjectError::class.java)

    }

    @Test
    fun `can get artefact hashes for report`()
    {
        insertReport("test", "version1")
        insertArtefact("version1", files = files)
        val sut = createSut()

        val result = sut.getArtefactHashes("test", "version1")

        assertThat(result["summary.csv"]).isNotNull()
        assertThat(result["graph.png"]).isNotNull()
    }

    @Test
    fun `getArtefactHashes does not get artefacts for wrong report`()
    {
        insertReport("test", "version1")
        insertReport("test", "version2")
        insertArtefact("version1", files = files)
        insertArtefact("version2", files = listOf(FileInfo("image.gif", 9876)))
        val sut = createSut()

        val result = sut.getArtefactHashes("test", "version2")

        assertThat(result.keys).containsExactlyElementsOf(listOf("image.gif"))
    }

}
