package org.vaccineimpact.orderlyweb.tests.database_tests

import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.orderlyweb.db.repositories.ArtefactRepository
import org.vaccineimpact.orderlyweb.db.repositories.OrderlyArtefactRepository
import org.vaccineimpact.orderlyweb.errors.UnknownObjectError
import org.vaccineimpact.orderlyweb.models.FileInfo
import org.vaccineimpact.orderlyweb.test_helpers.CleanDatabaseTests
import org.vaccineimpact.orderlyweb.test_helpers.insertReport
import org.vaccineimpact.orderlyweb.tests.insertArtefact

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

    @Test
    fun `can get artefacts for report`()
    {
        insertReport("test", "version1")

        insertArtefact("version1", description = "graph and summary",
                files = files)

        val files2 = listOf(FileInfo("img.gif", 1999))
        insertArtefact("version1", description = "animated gif",
                format = org.vaccineimpact.orderlyweb.models.ArtefactFormat.DATA,
                files = files2)

        val sut = createSut()

        val result = sut.getArtefacts("test", "version1")

        assertThat(result[0].description).isEqualTo("graph and summary")
        assertThat(result[0].files).hasSameElementsAs(files)
        assertThat(result[0].format).isEqualTo(org.vaccineimpact.orderlyweb.models.ArtefactFormat.REPORT)

        assertThat(result[1].description).isEqualTo("animated gif")
        assertThat(result[1].files).hasSameElementsAs(files2)
        assertThat(result[1].format).isEqualTo(org.vaccineimpact.orderlyweb.models.ArtefactFormat.DATA)
    }

    @Test
    fun `getArtefacts does not get artefacts for wrong report version`()
    {
        insertReport("test", "version1")
        insertReport("test", "version2")

        insertArtefact("version2", description = "graph and summary", files = files)

        val sut = createSut()
        val result = sut.getArtefacts("test", "version1")
        assertThat(result.count()).isEqualTo(0)
    }
}
