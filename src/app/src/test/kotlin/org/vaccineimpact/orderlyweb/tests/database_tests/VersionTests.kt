package org.vaccineimpact.orderlyweb.tests.database_tests

import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.orderlyweb.db.Orderly
import org.vaccineimpact.orderlyweb.db.OrderlyClient
import org.vaccineimpact.orderlyweb.errors.UnknownObjectError
import org.vaccineimpact.orderlyweb.models.*
import org.vaccineimpact.orderlyweb.test_helpers.*
import org.vaccineimpact.orderlyweb.tests.insertArtefact
import org.vaccineimpact.orderlyweb.tests.insertData
import org.vaccineimpact.orderlyweb.tests.insertFileInput
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
        insertReport("test", "version1", date = now,
                author = "dr author", requester = "ms requester")

        insertFileInput("version1", "file.csv", FilePurpose.RESOURCE, 2345)
        insertFileInput("version1", "graph.png", FilePurpose.RESOURCE, 3456)

        insertData("version1", "dat", "some sql", "testdb",  "somehash", 9876, 7654)

        insertArtefact("version1", "some artefact",
                ArtefactFormat.DATA, files = listOf(FileInfo("artefactfile.csv", 1234)))

        insertVersionParameterValues("version1", mapOf("p1" to "v1", "p2" to "v2"))

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
        assertThat(result.parameterValues.keys.count()).isEqualTo(2)
        assertThat(result.parameterValues["p1"]).isEqualTo("v1")
        assertThat(result.parameterValues["p2"]).isEqualTo("v2")
    }

    @Test
    fun `getTags returns all tags for version`()
    {
        val now = Timestamp(System.currentTimeMillis())
        insertReport("test", "version1", date = now,
                author = "dr author", requester = "ms requester", published = true)
        insertReportTags("test", "r1", "r2")
        insertVersionTags("version1", "v2", "v1")
        insertOrderlyTags("version1", "o1")

        val sut = createSut()
        val result = sut.getReportVersionTags("test", "version1")
        assertThat(result.versionTags).containsExactlyElementsOf(listOf("v1", "v2"))
        assertThat(result.reportTags).containsExactlyElementsOf(listOf("r1", "r2"))
        assertThat(result.orderlyTags).containsExactlyElementsOf(listOf("o1"))
    }

    @Test
    fun `getTags throws unknown object error if report version does not exist`()
    {
        val sut = createSut()
        Assertions.assertThatThrownBy { sut.getReportVersionTags("nonexistent", "version1") }
                .isInstanceOf(UnknownObjectError::class.java)
    }

    @Test
    fun `getTags throws unknown object error for reader if version is not published`()
    {
        insertReport("test", "version1", published = false)

        val sut = createSut()
        Assertions.assertThatThrownBy { sut.getReportVersionTags("test", "version1") }
                .isInstanceOf(UnknownObjectError::class.java)
    }

    @Test
    fun `getDetailsByNameAndVersion throws unknown object error if report version not published`()
    {
        insertReport("test", "version1", published = false)

        val sut = createSut()
        Assertions.assertThatThrownBy { sut.getDetailsByNameAndVersion("test", "version1") }
                .isInstanceOf(UnknownObjectError::class.java)
    }

    @Test
    fun `getDetailsByNameAndVersion throws unknown object error if report version doesnt exist`()
    {
        insertReport("test", "version1")

        val sut = createSut()

        Assertions.assertThatThrownBy { sut.getDetailsByNameAndVersion("test", "dsajkdsj") }
                .isInstanceOf(UnknownObjectError::class.java)
    }


    @Test
    fun `getDetailsByNameAndVersion throws unknown object error if report name not found`()
    {
        insertReport("test", "version1")

        val sut = createSut()

        Assertions.assertThatThrownBy { sut.getDetailsByNameAndVersion("dsajkdsj", "version") }
                .isInstanceOf(UnknownObjectError::class.java)
    }

    @Test
    fun `reviewer can get unpublished version details`()
    {
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
        insertReport("test", "va")

        insertReport("test", "vz")
        insertVersionParameterValues("vz", mapOf("p1" to "v1", "p2" to "v2"))

        insertReport("test2", "vc")
        insertReport("test2", "vb")
        insertReportWithCustomFields("test2", "vd", mapOf("author" to "test2 author"))
        insertReportWithCustomFields("test3", "test3version", mapOf())
        insertReport("test3", "test3versionunpublished", published = false)

        val sut = createSut()

        val results = sut.getAllReportVersions()

        Assertions.assertThat(results.count()).isEqualTo(6)

        Assertions.assertThat(results[0].name).isEqualTo("test")
        Assertions.assertThat(results[0].displayName).isEqualTo("display name test")
        Assertions.assertThat(results[0].latestVersion).isEqualTo("vz")
        Assertions.assertThat(results[0].id).isEqualTo("va")
        Assertions.assertThat(results[0].published).isTrue()
        Assertions.assertThat(results[0].customFields.keys.count()).isEqualTo(2)
        Assertions.assertThat(results[0].customFields["author"]).isEqualTo("author authorson")
        Assertions.assertThat(results[0].customFields["requester"]).isEqualTo("requester mcfunder")
        Assertions.assertThat(results[0].parameterValues.keys.count()).isEqualTo(0)

        Assertions.assertThat(results[1].name).isEqualTo("test")
        Assertions.assertThat(results[1].id).isEqualTo("vz")
        Assertions.assertThat(results[1].latestVersion).isEqualTo("vz")
        Assertions.assertThat(results[1].parameterValues.keys.count()).isEqualTo(2)
        Assertions.assertThat(results[1].parameterValues["p1"]).isEqualTo("v1")
        Assertions.assertThat(results[1].parameterValues["p2"]).isEqualTo("v2")

        Assertions.assertThat(results[2].name).isEqualTo("test2")
        Assertions.assertThat(results[2].id).isEqualTo("vb")
        Assertions.assertThat(results[2].latestVersion).isEqualTo("vd")

        Assertions.assertThat(results[3].name).isEqualTo("test2")
        Assertions.assertThat(results[3].id).isEqualTo("vc")
        Assertions.assertThat(results[3].latestVersion).isEqualTo("vd")

        Assertions.assertThat(results[4].name).isEqualTo("test2")
        Assertions.assertThat(results[4].id).isEqualTo("vd")
        Assertions.assertThat(results[4].latestVersion).isEqualTo("vd")
        Assertions.assertThat(results[4].customFields.keys.count()).isEqualTo(2)
        Assertions.assertThat(results[4].customFields["author"]).isEqualTo("test2 author")
        Assertions.assertThat(results[4].customFields["requester"]).isEqualTo(null)

        Assertions.assertThat(results[5].name).isEqualTo("test3")
        Assertions.assertThat(results[5].id).isEqualTo("test3version")
        Assertions.assertThat(results[5].latestVersion).isEqualTo("test3version")
        Assertions.assertThat(results[5].customFields.keys.count()).isEqualTo(2)
        Assertions.assertThat(results[5].customFields["author"]).isEqualTo(null)
        Assertions.assertThat(results[5].customFields["requester"]).isEqualTo(null)

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
        insertReport("test1", "20170103-143015-1234pub")
        insertReport("test1", "20180103-143015-1234pub")
        insertReport("test1", "20190103-143015-1234unpub", published = false)

        insertReport("test2", "20160203-143015-1234pub")

        insertReport("test3", "20170203-143015-1234pub")

        insertReport("test4", "20180203-143015-1234unpub", published = false)

        insertGlobalPinnedReport("test4", 0)
        insertGlobalPinnedReport("test3", 1)
        insertGlobalPinnedReport("test1", 2)

        val sut = createSut(isReviewer = false)

        val results = sut.getGlobalPinnedReports()

        assertThat(results.count()).isEqualTo(2)
        assertThat(results[0].name).isEqualTo("test3")
        assertThat(results[0].latestVersion).isEqualTo("20170203-143015-1234pub")
        assertThat(results[1].name).isEqualTo("test1")
        assertThat(results[1].latestVersion).isEqualTo("20180103-143015-1234pub")
    }

    @Test
    fun `reviewer can get latest published and unpublished versions of pinned reports`()
    {
        insertReport("test1", "20170103-143015-1234pub")
        insertReport("test1", "20180103-143015-1234pub")
        insertReport("test1", "20190103-143015-1234unpub", published = false)

        insertReport("test2", "20160203-143015-1234pub")

        insertReport("test3", "20170203-143015-1234pub")

        insertReport("test4", "20180203-143015-1234unpub", published = false)

        insertGlobalPinnedReport("test4", 0)
        insertGlobalPinnedReport("test3", 1)
        insertGlobalPinnedReport("test1", 2)

        val sut = createSut(isReviewer = true)

        val results = sut.getGlobalPinnedReports()

        assertThat(results.count()).isEqualTo(3)
        assertThat(results[0].name).isEqualTo("test4")
        assertThat(results[0].latestVersion).isEqualTo("20180203-143015-1234unpub")
        assertThat(results[1].name).isEqualTo("test3")
        assertThat(results[1].latestVersion).isEqualTo("20170203-143015-1234pub")
        assertThat(results[2].name).isEqualTo("test1")
        assertThat(results[2].latestVersion).isEqualTo("20190103-143015-1234unpub")
    }
}