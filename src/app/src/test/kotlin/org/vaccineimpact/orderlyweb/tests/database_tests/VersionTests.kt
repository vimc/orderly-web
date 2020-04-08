package org.vaccineimpact.orderlyweb.tests.database_tests

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.orderlyweb.db.Orderly
import org.vaccineimpact.orderlyweb.db.OrderlyClient
import org.vaccineimpact.orderlyweb.db.repositories.ReportRepository
import org.vaccineimpact.orderlyweb.db.repositories.TagRepository
import org.vaccineimpact.orderlyweb.models.*
import org.vaccineimpact.orderlyweb.test_helpers.*
import org.vaccineimpact.orderlyweb.tests.insertArtefact
import org.vaccineimpact.orderlyweb.tests.insertData
import org.vaccineimpact.orderlyweb.tests.insertFileInput
import java.sql.Timestamp
import java.time.Instant

class VersionTests : CleanDatabaseTests()
{
    private fun createSut(isReviewer: Boolean = false): OrderlyClient
    {
        return Orderly(isReviewer, true, listOf())
    }

    @Test
    fun `can get report version details`()
    {
        val now = Timestamp(System.currentTimeMillis())
        insertReport("test", "version1", date = now,
                author = "dr author", requester = "ms requester")

        insertFileInput("version1", "file.csv", FilePurpose.RESOURCE, 2345)
        insertFileInput("version1", "graph.png", FilePurpose.RESOURCE, 3456)

        insertData("version1", "dat", "some sql", "testdb", "somehash", 9876, 7654)

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
        val mockTagRepo = mock<TagRepository> {
            on { getVersionTags(listOf("v1")) } doReturn
                    mapOf("v1" to listOf("b-tag", "a-tag"))

            on { getReportTagsForVersions(listOf("v1")) } doReturn
                    mapOf("v1" to listOf("b-tag", "d-tag"), "v2" to listOf("bb"))

            on { getOrderlyTagsForVersions(listOf("v1")) } doReturn
                    mapOf("v1" to listOf("c-tag"), "v2" to listOf("aa"))
        }

        val sut = Orderly(isReviewer = true,
                isGlobalReader = true,
                reportReadingScopes = listOf(),
                reportRepository = mock(),
                tagRepository = mockTagRepo)

        val result = sut.getReportVersionTags("test", "v1")
        assertThat(result.versionTags).containsExactlyElementsOf(listOf("a-tag", "b-tag"))
        assertThat(result.reportTags).containsExactlyElementsOf(listOf("b-tag", "d-tag"))
        assertThat(result.orderlyTags).containsExactlyElementsOf(listOf("c-tag"))
    }

    @Test
    fun `getTags checks report version exists and is accessible`()
    {
        val mockReportRepo = mock<ReportRepository>()
        val sut = Orderly(isReviewer = true,
                isGlobalReader = true,
                reportReadingScopes = listOf(),
                reportRepository = mockReportRepo,
                tagRepository = mock())

        sut.getReportVersionTags("r1", "version1")
        verify(mockReportRepo).getReportVersion("r1", "version1")
    }

    @Test
    fun `getAllReportVersions returns version, report and orderly tags`()
    {
        val basicReportVersion = BasicReportVersion("report", "display name", "v1", true,
                Instant.now(), "v3", "description")

        val mockTagRepo = mock<TagRepository> {
            on { getVersionTags(listOf("v1", "v2", "v3")) } doReturn
                    mapOf("v1" to listOf("b-tag", "a-tag"))

            on { getReportTagsForVersions(listOf("v1", "v2", "v3")) } doReturn
                    mapOf("v1" to listOf("b-tag", "d-tag"), "v2" to listOf("bb"))

            on { getOrderlyTagsForVersions(listOf("v1", "v2", "v3")) } doReturn
                    mapOf("v1" to listOf("b-tag", "d-tag"), "v2" to listOf("aa"))
        }

        val mockReportRepo = mock<ReportRepository> {
            on { getAllReportVersions() } doReturn listOf(
                    basicReportVersion, basicReportVersion.copy(id = "v2"), basicReportVersion.copy(id = "v3")
            )
        }

        val sut = Orderly(isReviewer = true,
                isGlobalReader = true,
                reportReadingScopes = listOf(),
                reportRepository = mockReportRepo,
                tagRepository = mockTagRepo)

        val results = sut.getAllReportVersions()

        assertThat(results[0].id).isEqualTo("v1")
        //tags should be sorted and distinct
        assertThat(results[0].tags).containsExactlyElementsOf(listOf("a-tag", "b-tag", "d-tag"))

        assertThat(results[1].id).isEqualTo("v2")
        assertThat(results[1].tags).containsExactlyElementsOf(listOf("aa", "bb"))

        assertThat(results[2].id).isEqualTo("v3")
        assertThat(results[2].tags.count()).isEqualTo(0)
    }

    @Test
    fun `can get report versions`()
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

        assertThat(results.count()).isEqualTo(6)

        assertThat(results[0].name).isEqualTo("test")
        assertThat(results[0].displayName).isEqualTo("display name test")
        assertThat(results[0].latestVersion).isEqualTo("vz")
        assertThat(results[0].id).isEqualTo("va")
        assertThat(results[0].published).isTrue()
        assertThat(results[0].customFields.keys.count()).isEqualTo(2)
        assertThat(results[0].customFields["author"]).isEqualTo("author authorson")
        assertThat(results[0].customFields["requester"]).isEqualTo("requester mcfunder")
        assertThat(results[0].parameterValues.keys.count()).isEqualTo(0)

        assertThat(results[1].name).isEqualTo("test")
        assertThat(results[1].id).isEqualTo("vz")
        assertThat(results[1].latestVersion).isEqualTo("vz")
        assertThat(results[1].parameterValues.keys.count()).isEqualTo(2)
        assertThat(results[1].parameterValues["p1"]).isEqualTo("v1")
        assertThat(results[1].parameterValues["p2"]).isEqualTo("v2")

        assertThat(results[2].name).isEqualTo("test2")
        assertThat(results[2].id).isEqualTo("vb")
        assertThat(results[2].latestVersion).isEqualTo("vd")

        assertThat(results[3].name).isEqualTo("test2")
        assertThat(results[3].id).isEqualTo("vc")
        assertThat(results[3].latestVersion).isEqualTo("vd")

        assertThat(results[4].name).isEqualTo("test2")
        assertThat(results[4].id).isEqualTo("vd")
        assertThat(results[4].latestVersion).isEqualTo("vd")
        assertThat(results[4].customFields.keys.count()).isEqualTo(2)
        assertThat(results[4].customFields["author"]).isEqualTo("test2 author")
        assertThat(results[4].customFields["requester"]).isEqualTo(null)

        assertThat(results[5].name).isEqualTo("test3")
        assertThat(results[5].id).isEqualTo("test3version")
        assertThat(results[5].latestVersion).isEqualTo("test3version")
        assertThat(results[5].customFields.keys.count()).isEqualTo(2)
        assertThat(results[5].customFields["author"]).isEqualTo(null)
        assertThat(results[5].customFields["requester"]).isEqualTo(null)

    }

}