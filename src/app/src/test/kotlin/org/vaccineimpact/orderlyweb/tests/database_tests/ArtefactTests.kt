package org.vaccineimpact.orderlyweb.tests.database_tests

import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.vaccineimpact.orderlyweb.db.Orderly
import org.vaccineimpact.orderlyweb.errors.UnknownObjectError
import org.vaccineimpact.orderlyweb.test_helpers.CleanDatabaseTests
import org.vaccineimpact.orderlyweb.tests.insertArtefact
import org.vaccineimpact.orderlyweb.test_helpers.insertReport

class ArtefactTests : CleanDatabaseTests()
{

    private fun createSut(isReviewer: Boolean = false): Orderly
    {
        return Orderly(isReviewer, true, listOf())
    }

    @Test
    fun `getArtefactHash returns artefact hash if report has artefact`()
    {
        insertReport("test", "version1")
        insertArtefact("version1", fileNames = listOf("summary.csv", "graph.png"))

        val sut = createSut()
        val result = sut.getArtefactHash("test", "version1", "summary.csv")

        assertThat(result).isNotNull()
    }

    @Test
    fun `getArtefactHash throws unknown object error if report does not have artefact`()
    {
        insertReport("test", "version1")
        insertReport("test", "version2")
        insertArtefact("version1", fileNames = listOf("summary.csv", "graph.png"))

        val sut = createSut()

        Assertions.assertThatThrownBy { sut.getArtefactHash("test", "version1", "details.csv") }
                .isInstanceOf(UnknownObjectError::class.java)

    }

    @Test
    fun `getArtefactHash throws unknown object error if report not published`()
    {
        insertReport("test", "version1", published = false)
        insertArtefact("version1", fileNames = listOf("summary.csv", "graph.png"))

        val sut = createSut()

        Assertions.assertThatThrownBy { sut.getArtefactHash("test", "version1", "graph.png") }
                .isInstanceOf(UnknownObjectError::class.java)

    }

    @Test
    fun `can get artefact hashes for report`()
    {
        insertReport("test", "version1")
        insertArtefact("version1", fileNames = listOf("summary.csv", "graph.png"))
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
        insertArtefact("version1", fileNames = listOf("summary.csv", "graph.png"))
        insertArtefact("version2", fileNames = listOf("image.gif"))
        val sut = createSut()

        val result = sut.getArtefactHashes("test", "version2")

        assertThat(result.keys).containsExactlyElementsOf(listOf("image.gif"))
    }

    @Test
    fun `can get artefacts for report`()
    {
        insertReport("test", "version1")

        insertArtefact("version1", description = "graph and summary",
                fileNames = listOf("graph.png", "summary.csv"))

        insertArtefact("version1", description = "animated gif",
                format = org.vaccineimpact.orderlyweb.models.ArtefactFormat.DATA,
                fileNames = listOf("img.gif"))

        val sut = createSut()

        val result = sut.getArtefacts("test", "version1")

        assertThat(result[0].description).isEqualTo("graph and summary")
        assertThat(result[0].files).hasSameElementsAs(listOf("graph.png", "summary.csv"))
        assertThat(result[0].format).isEqualTo(org.vaccineimpact.orderlyweb.models.ArtefactFormat.REPORT)

        assertThat(result[1].description).isEqualTo("animated gif")
        assertThat(result[1].files).hasSameElementsAs(listOf("img.gif"))
        assertThat(result[1].format).isEqualTo(org.vaccineimpact.orderlyweb.models.ArtefactFormat.DATA)
    }

    @Test
    fun `getArtefacts does not get artefacts for wrong report version`()
    {
        insertReport("test", "version1")
        insertReport("test", "version2")

        insertArtefact("version2", description = "graph and summary",
                fileNames = listOf("graph.png", "summary.csv"))

        val sut = createSut()
        val result = sut.getArtefacts("test", "version1")
        assertThat(result.count()).isEqualTo(0)
    }

    @Test
    fun `getArtefacts throws UnknownObjectError if version does not belong to report`()
    {
        insertReport("test", "v1")
        insertReport("badreport", "badversion")

        val sut = createSut()
        assertThatThrownBy { sut.getArtefacts("badreport", "v1") }
                .isInstanceOf(UnknownObjectError::class.java)
    }

    @Test
    fun `getArtefacts throws UnknownObjectError if report is unpublished and user is not a reviewer`()
    {
        insertReport("test", "v1", published = false)

        val sut = createSut()
        assertThatThrownBy { sut.getArtefacts("test", "v1") }
                .isInstanceOf(UnknownObjectError::class.java)
    }

    @Test
    fun `reviewer can getArtefacts for unpublished report`()
    {
        insertReport("test", "v1", published = false)

        val sut = createSut(isReviewer = true)
        sut.getArtefacts("test", "v1")
    }

}
