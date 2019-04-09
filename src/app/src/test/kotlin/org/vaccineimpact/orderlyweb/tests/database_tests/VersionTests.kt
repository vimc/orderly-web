package org.vaccineimpact.orderlyweb.tests.database_tests

import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.orderlyweb.models.Artefact
import org.vaccineimpact.orderlyweb.models.ArtefactFormat
import org.vaccineimpact.orderlyweb.models.FilePurpose
import org.vaccineimpact.orderlyweb.db.Orderly
import org.vaccineimpact.orderlyweb.errors.UnknownObjectError
import org.vaccineimpact.orderlyweb.test_helpers.CleanDatabaseTests
import org.vaccineimpact.orderlyweb.tests.insertArtefact
import org.vaccineimpact.orderlyweb.tests.insertData
import org.vaccineimpact.orderlyweb.tests.insertFileInput
import org.vaccineimpact.orderlyweb.test_helpers.insertReport
import java.sql.Timestamp

class VersionTests : CleanDatabaseTests()
{

    @Test
    fun `reader can get published report version details`()
    {
        val now = Timestamp(System.currentTimeMillis())
        insertReport("test", "version1", date = now,
                author = "dr author", requester = "ms requester")

        insertFileInput("version1", "file.csv", FilePurpose.RESOURCE)
        insertFileInput("version1", "graph.png", FilePurpose.RESOURCE)

        insertData("version1", "dat", "some sql", "somehash")

        insertArtefact("version1", "some artefact",
                ArtefactFormat.DATA, fileNames = listOf("artefactfile.csv"))

        val sut = Orderly()
        val result = sut.getDetailsByNameAndVersion("test", "version1")

        assertThat(result.id).isEqualTo("version1")
        assertThat(result.name).isEqualTo("test")
        assertThat(result.displayName).isEqualTo("display name test")
        assertThat(result.author).isEqualTo("dr author")
        assertThat(result.requester).isEqualTo("ms requester")
        assertThat(result.date).isEqualTo(now.toInstant())
        assertThat(result.published).isTrue()
        assertThat(result.resources).hasSameElementsAs(listOf("file.csv", "graph.png"))
        assertThat(result.artefacts).containsExactly(Artefact(ArtefactFormat.DATA,
                "some artefact", listOf("artefactfile.csv")))
        assertThat(result.dataHashes.keys).containsExactly("dat")
        assertThat(result.dataHashes["dat"]).isEqualTo("somehash")

    }

    @Test
    fun `getDetailsByNameAndVersion throws unknown object error if report version not published`()
    {
        insertReport("test", "version1", published = false)

        val sut = Orderly()
        Assertions.assertThatThrownBy { sut.getDetailsByNameAndVersion("test", "version1") }
                .isInstanceOf(UnknownObjectError::class.java)
    }

    @Test
    fun `getDetailsByNameAndVersion throws unknown object error if report version doesnt exist`()
    {
        insertReport("test", "version1")

        val sut = Orderly()

        Assertions.assertThatThrownBy { sut.getDetailsByNameAndVersion("test", "dsajkdsj") }
                .isInstanceOf(UnknownObjectError::class.java)
    }


    @Test
    fun `getDetailsByNameAndVersion throws unknown object error if report name not found`()
    {
        insertReport("test", "version1")

        val sut = Orderly()

        Assertions.assertThatThrownBy { sut.getDetailsByNameAndVersion("dsajkdsj", "version") }
                .isInstanceOf(UnknownObjectError::class.java)
    }

    @Test
    fun `reviewer can get unpublished version details`()
    {
        insertReport("test", "version1", published = false)

        val sut = Orderly(isReviewer = true)

        val result = sut.getDetailsByNameAndVersion("test", "version1")

        assertThat(result.name).isEqualTo("test")
        assertThat(result.id).isEqualTo("version1")
        assertThat(result.published).isFalse()
    }


    @Test
    fun `reader can get all published report versions`()
    {
        insertReport("test", "va")
        insertReport("test", "vz")
        insertReport("test2", "vc")
        insertReport("test2", "vb")
        insertReport("test2", "vd")
        insertReport("test3", "test3version")
        insertReport("test3", "test3versionunpublished", published = false)

        val sut = Orderly()

        val results = sut.getAllReportVersions()

        Assertions.assertThat(results.count()).isEqualTo(6)

        Assertions.assertThat(results[0].name).isEqualTo("test")
        Assertions.assertThat(results[0].displayName).isEqualTo("display name test")
        Assertions.assertThat(results[0].latestVersion).isEqualTo("vz")
        Assertions.assertThat(results[0].id).isEqualTo("va")
        Assertions.assertThat(results[0].published).isTrue()
        Assertions.assertThat(results[0].author).isEqualTo("author authorson")
        Assertions.assertThat(results[0].requester).isEqualTo("requester mcfunder")

        Assertions.assertThat(results[1].name).isEqualTo("test")
        Assertions.assertThat(results[1].id).isEqualTo("vz")
        Assertions.assertThat(results[1].latestVersion).isEqualTo("vz")

        Assertions.assertThat(results[2].name).isEqualTo("test2")
        Assertions.assertThat(results[2].id).isEqualTo("vb")
        Assertions.assertThat(results[2].latestVersion).isEqualTo("vd")

        Assertions.assertThat(results[3].name).isEqualTo("test2")
        Assertions.assertThat(results[3].id).isEqualTo("vc")
        Assertions.assertThat(results[3].latestVersion).isEqualTo("vd")

        Assertions.assertThat(results[4].name).isEqualTo("test2")
        Assertions.assertThat(results[4].id).isEqualTo("vd")
        Assertions.assertThat(results[4].latestVersion).isEqualTo("vd")

        Assertions.assertThat(results[5].name).isEqualTo("test3")
        Assertions.assertThat(results[5].id).isEqualTo("test3version")
        Assertions.assertThat(results[5].latestVersion).isEqualTo("test3version")

    }

    @Test
    fun `reviewer can get all published and unpublished report versions`()
    {
        insertReport("test", "va")
        insertReport("test", "vz")
        insertReport("test2", "vc")
        insertReport("test2", "vb")
        insertReport("test2", "vd")
        insertReport("test3", "test3version")
        insertReport("test3", "test3versionunpublished", published = false)

        val sut = Orderly(isReviewer = true)

        val results = sut.getAllReportVersions()

        Assertions.assertThat(results.count()).isEqualTo(7)

        Assertions.assertThat(results[0].name).isEqualTo("test")
        Assertions.assertThat(results[0].displayName).isEqualTo("display name test")
        Assertions.assertThat(results[0].latestVersion).isEqualTo("vz")
        Assertions.assertThat(results[0].id).isEqualTo("va")
        Assertions.assertThat(results[0].published).isTrue()
        Assertions.assertThat(results[0].author).isEqualTo("author authorson")
        Assertions.assertThat(results[0].requester).isEqualTo("requester mcfunder")

        Assertions.assertThat(results[1].name).isEqualTo("test")
        Assertions.assertThat(results[1].id).isEqualTo("vz")
        Assertions.assertThat(results[1].latestVersion).isEqualTo("vz")

        Assertions.assertThat(results[2].name).isEqualTo("test2")
        Assertions.assertThat(results[2].id).isEqualTo("vb")
        Assertions.assertThat(results[2].latestVersion).isEqualTo("vd")

        Assertions.assertThat(results[3].name).isEqualTo("test2")
        Assertions.assertThat(results[3].id).isEqualTo("vc")
        Assertions.assertThat(results[3].latestVersion).isEqualTo("vd")

        Assertions.assertThat(results[4].name).isEqualTo("test2")
        Assertions.assertThat(results[4].id).isEqualTo("vd")
        Assertions.assertThat(results[4].latestVersion).isEqualTo("vd")

        Assertions.assertThat(results[5].name).isEqualTo("test3")
        Assertions.assertThat(results[5].id).isEqualTo("test3version")
        Assertions.assertThat(results[5].latestVersion).isEqualTo("test3versionunpublished")

        Assertions.assertThat(results[6].name).isEqualTo("test3")
        Assertions.assertThat(results[6].id).isEqualTo("test3versionunpublished")
        Assertions.assertThat(results[6].latestVersion).isEqualTo("test3versionunpublished")
    }
}