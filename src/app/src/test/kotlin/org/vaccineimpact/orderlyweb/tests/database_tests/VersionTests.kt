package org.vaccineimpact.orderlyweb.tests.database_tests

import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.orderlyweb.db.Orderly
import org.vaccineimpact.orderlyweb.db.OrderlyClient
import org.vaccineimpact.orderlyweb.errors.UnknownObjectError
import org.vaccineimpact.orderlyweb.models.*
import org.vaccineimpact.orderlyweb.test_helpers.CleanDatabaseTests
import org.vaccineimpact.orderlyweb.test_helpers.insertCustomFields
import org.vaccineimpact.orderlyweb.test_helpers.insertGlobalPinnedReport
import org.vaccineimpact.orderlyweb.tests.insertArtefact
import org.vaccineimpact.orderlyweb.tests.insertData
import org.vaccineimpact.orderlyweb.tests.insertFileInput
import org.vaccineimpact.orderlyweb.test_helpers.insertReport
import java.sql.Timestamp

class VersionTests : CleanDatabaseTests()
{
    private fun createSut(isReviewer: Boolean = false): OrderlyClient
    {
        return Orderly(isReviewer, true, listOf())
    }

    @Test
    fun `reader can get published report version details`()
    {
        val now = Timestamp(System.currentTimeMillis())
        insertCustomFields()
        insertReport("test", "version1", date = now,
                author = "dr author", requester = "ms requester")

        insertFileInput("version1", "file.csv", FilePurpose.RESOURCE, 2345)
        insertFileInput("version1", "graph.png", FilePurpose.RESOURCE, 3456)

        insertData("version1", "dat", "some sql", "testdb",  "somehash", 9876, 7654)

        insertArtefact("version1", "some artefact",
                ArtefactFormat.DATA, files = listOf(FileInfo("artefactfile.csv", 1234)))

        val sut = createSut()
        val result = sut.getDetailsByNameAndVersion("test", "version1")

        assertThat(result.id).isEqualTo("version1")
        assertThat(result.name).isEqualTo("test")
        assertThat(result.displayName).isEqualTo("display name test")
        assertThat(result.date).isEqualTo(now.toInstant())
        assertThat(result.published).isTrue()
        assertThat(result.resources).hasSameElementsAs(listOf(FileInfo("file.csv", 2345), FileInfo("graph.png", 3456)))
        assertThat(result.artefacts).containsExactly(Artefact(ArtefactFormat.DATA,
                "some artefact", listOf(FileInfo("artefactfile.csv", 1234))))
        assertThat(result.dataInfo).hasSameElementsAs(listOf(DataInfo("dat", 9876, 7654)))
    }

    @Test
    fun `getDetailsByNameAndVersion throws unknown object error if report version not published`()
    {
        insertCustomFields()
        insertReport("test", "version1", published = false)

        val sut = createSut()
        Assertions.assertThatThrownBy { sut.getDetailsByNameAndVersion("test", "version1") }
                .isInstanceOf(UnknownObjectError::class.java)
    }

    @Test
    fun `getDetailsByNameAndVersion throws unknown object error if report version doesnt exist`()
    {
        insertCustomFields()
        insertReport("test", "version1")

        val sut = createSut()

        Assertions.assertThatThrownBy { sut.getDetailsByNameAndVersion("test", "dsajkdsj") }
                .isInstanceOf(UnknownObjectError::class.java)
    }


    @Test
    fun `getDetailsByNameAndVersion throws unknown object error if report name not found`()
    {
        insertCustomFields()
        insertReport("test", "version1")

        val sut = createSut()

        Assertions.assertThatThrownBy { sut.getDetailsByNameAndVersion("dsajkdsj", "version") }
                .isInstanceOf(UnknownObjectError::class.java)
    }

    @Test
    fun `reviewer can get unpublished version details`()
    {
        insertCustomFields()
        insertReport("test", "version1", published = false)

        val sut = createSut(isReviewer = true)

        val result = sut.getDetailsByNameAndVersion("test", "version1")

        assertThat(result.name).isEqualTo("test")
        assertThat(result.id).isEqualTo("version1")
        assertThat(result.published).isFalse()
    }

    @Test
    fun `reader can get all published report versions`()
    {
        insertCustomFields()
        insertReport("test", "va")
        insertReport("test", "vz")
        insertReport("test2", "vc")
        insertReport("test2", "vb")
        insertReport("test2", "vd")
        insertReport("test3", "test3version")
        insertReport("test3", "test3versionunpublished", published = false)

        val sut = createSut()

        val results = sut.getAllReportVersions()

        Assertions.assertThat(results.count()).isEqualTo(6)

        Assertions.assertThat(results[0].name).isEqualTo("test")
        Assertions.assertThat(results[0].displayName).isEqualTo("display name test")
        Assertions.assertThat(results[0].latestVersion).isEqualTo("vz")
        Assertions.assertThat(results[0].id).isEqualTo("va")
        Assertions.assertThat(results[0].published).isTrue()
        Assertions.assertThat(results[0].customFields["author"]).isEqualTo("author authorson")
        Assertions.assertThat(results[0].customFields["requester"]).isEqualTo("requester mcfunder")

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
        insertCustomFields()
        insertReport("test", "va")
        insertReport("test", "vz")
        insertReport("test2", "vc")
        insertReport("test2", "vb")
        insertReport("test2", "vd")
        insertReport("test3", "test3version")
        insertReport("test3", "test3versionunpublished", published = false)

        val sut = createSut(isReviewer = true)

        val results = sut.getAllReportVersions()

        Assertions.assertThat(results.count()).isEqualTo(7)

        Assertions.assertThat(results[0].name).isEqualTo("test")
        Assertions.assertThat(results[0].displayName).isEqualTo("display name test")
        Assertions.assertThat(results[0].latestVersion).isEqualTo("vz")
        Assertions.assertThat(results[0].id).isEqualTo("va")
        Assertions.assertThat(results[0].published).isTrue()
        Assertions.assertThat(results[0].customFields["author"]).isEqualTo("author authorson")
        Assertions.assertThat(results[0].customFields["requester"]).isEqualTo("requester mcfunder")

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

    @Test
    fun `reader can get latest published versions of pinned reports`()
    {
        insertCustomFields()
        insertReport("test1", "test1_1_pub")
        insertReport("test1", "test1_2_pub")
        insertReport("test1", "test1_3_unpub", published = false)

        insertReport("test2", "test2_1_pub")

        insertReport("test3", "test3_1_pub")

        insertReport("test4", "test4_1_unpub", published = false)

        insertGlobalPinnedReport("test4", 0)
        insertGlobalPinnedReport("test3", 1)
        insertGlobalPinnedReport("test1", 2)

        val sut = createSut(isReviewer = false)

        val results = sut.getGlobalPinnedReports()

        assertThat(results.count()).isEqualTo(2)
        assertThat(results[0].name).isEqualTo("test3")
        assertThat(results[0].id).isEqualTo("test3_1_pub")
        assertThat(results[1].name).isEqualTo("test1")
        assertThat(results[1].id).isEqualTo("test1_2_pub")
    }

    @Test
    fun `reviewer can get latest published and unpublished versions of pinned reports`()
    {
        insertCustomFields()
        insertReport("test1", "test1_1_pub")
        insertReport("test1", "test1_2_pub")
        insertReport("test1", "test1_3_unpub", published = false)

        insertReport("test2", "test2_1_pub")

        insertReport("test3", "test3_1_pub")

        insertReport("test4", "test4_1_unpub", published = false)

        insertGlobalPinnedReport("test4", 0)
        insertGlobalPinnedReport("test3", 1)
        insertGlobalPinnedReport("test1", 2)

        val sut = createSut(isReviewer = true)

        val results = sut.getGlobalPinnedReports()

        assertThat(results.count()).isEqualTo(3)
        assertThat(results[0].name).isEqualTo("test4")
        assertThat(results[0].id).isEqualTo("test4_1_unpub")
        assertThat(results[1].name).isEqualTo("test3")
        assertThat(results[1].id).isEqualTo("test3_1_pub")
        assertThat(results[2].name).isEqualTo("test1")
        assertThat(results[2].id).isEqualTo("test1_3_unpub")
    }
}