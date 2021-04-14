package org.vaccineimpact.orderlyweb.tests.database_tests

import com.nhaarman.mockito_kotlin.doThrow
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.vaccineimpact.orderlyweb.db.Orderly
import org.vaccineimpact.orderlyweb.db.OrderlyClient
import org.vaccineimpact.orderlyweb.db.repositories.ReportRepository
import org.vaccineimpact.orderlyweb.db.repositories.TagRepository
import org.vaccineimpact.orderlyweb.errors.UnknownObjectError
import org.vaccineimpact.orderlyweb.models.*
import org.vaccineimpact.orderlyweb.test_helpers.CleanDatabaseTests
import org.vaccineimpact.orderlyweb.test_helpers.insertReport
import org.vaccineimpact.orderlyweb.tests.insertArtefact
import org.vaccineimpact.orderlyweb.tests.insertData
import org.vaccineimpact.orderlyweb.tests.insertFileInput
import java.time.Instant

// TODO once data and resources moved out this should no longer be a database test class
// and should be moved to unit tests
class OrderlyTests : CleanDatabaseTests()
{
    val now = Instant.now()

    private val basicReportVersion =
            ReportVersionWithDescLatest(
                    "test",
                    "display name",
                    "v1",
                    true,
                    now,
                    "vz",
                    "descripion")

    private val basicReportVersionElapsed =
            ReportVersionWithDescLatestElapsed(
                    "test",
                    "display name",
                    "v1",
                    true,
                    now,
                    "vz",
                    "descripion",
                    1.5,
                    "master",
                    "abc123")

    private val mockReportRepo = mock<ReportRepository> {
        on { getReportVersion("test", "v1") } doReturn basicReportVersionElapsed
        on { getAllReportVersions() } doReturn listOf(basicReportVersion, basicReportVersion.copy(id = "v2"))
        on { getParametersForVersions(listOf("v1")) } doReturn mapOf("v1" to mapOf("p1" to "param1", "p2" to "param2"))
        on { getLatestVersion("test") } doReturn basicReportVersion.copy(id = "latest", date = now.minusSeconds(100))
        on { getDatedChangelogForReport("test", now.minusSeconds(100)) } doReturn
                listOf(Changelog("v1", "public", "getLatestChangelog", true, true))
        on { getDatedChangelogForReport("test", now) } doReturn
                listOf(Changelog("v1", "public", "getByNameAndVersion", true, true))
    }

    private fun createSut(isReviewer: Boolean = false): OrderlyClient
    {
        return Orderly(isReviewer, true, listOf(), reportRepository = mockReportRepo, tagRepository = mock())
    }

    @Test
    fun `can get report version details`()
    {
        // although fetching reports from the repo is mocked we need to insert this
        // record so that we can insert files/data/artefacts/parameters which are
        // still fetched directly from the db
        insertReport("test", "v1")

        insertFileInput("v1", "file.csv", FilePurpose.RESOURCE, 2345)
        insertFileInput("v1", "graph.png", FilePurpose.RESOURCE, 3456)

        insertData("v1", "dat", "some sql", "testdb", "somehash", 9876, 7654)

        insertArtefact("v1", "some artefact",
                ArtefactFormat.DATA, files = listOf(FileInfo("artefactfile.csv", 1234)))

        val sut = createSut()
        val result = sut.getDetailsByNameAndVersion("test", "v1")

        // these properties come from the mock
        assertThat(result.id).isEqualTo("v1")
        assertThat(result.name).isEqualTo("test")
        assertThat(result.displayName).isEqualTo("display name")
        assertThat(result.date).isEqualTo(now)
        assertThat(result.published).isTrue()
        assertThat(result.parameterValues.keys.count()).isEqualTo(2)
        assertThat(result.parameterValues["p1"]).isEqualTo("param1")
        assertThat(result.parameterValues["p2"]).isEqualTo("param2")

        // these come from the db
        assertThat(result.resources).hasSameElementsAs(listOf(FileInfo("file.csv", 2345), FileInfo("graph.png", 3456)))
        assertThat(result.artefacts).containsExactly(Artefact(ArtefactFormat.DATA,
                "some artefact", listOf(FileInfo("artefactfile.csv", 1234))))
        assertThat(result.dataInfo).hasSameElementsAs(listOf(DataInfo("dat", 9876, 7654)))
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
    fun `getTags checks report version exists`()
    {
        val mockReportRepo = mock<ReportRepository> {
            on { getReportVersion("test", "v1") } doThrow UnknownObjectError("report", "test")
        }

        val sut = Orderly(isReviewer = true,
                isGlobalReader = true,
                reportReadingScopes = listOf(),
                reportRepository = mockReportRepo,
                tagRepository = mock())

        assertThatThrownBy { sut.getReportVersionTags("test", "v1") }
                .isInstanceOf(UnknownObjectError::class.java)
    }


    @Test
    fun `getAllReportVersions returns version, report and orderly tags`()
    {
        val basicReportVersion = ReportVersionWithDescLatest("report", "display name", "v1", true,
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
    fun `can getAllReportVersions with custom fields and parameters`()
    {
        val versionIds = listOf("v1", "v2")

        val mockReportRepo = mock<ReportRepository> {
            on { getAllReportVersions() } doReturn listOf(
                    basicReportVersion,
                    basicReportVersion.copy(id = "v2"))
            on { getAllCustomFields() } doReturn
                    mapOf("author" to null, "requester" to null)
            on { getCustomFieldsForVersions(versionIds) } doReturn
                    mapOf("v1" to mapOf("author" to "author authorson"))
            on { getParametersForVersions(versionIds) } doReturn
                    mapOf("v1" to mapOf("p1" to "param1"),
                            "v2" to mapOf("p2" to "param2"))
        }

        val sut = Orderly(isReviewer = true,
                isGlobalReader = true,
                reportReadingScopes = listOf(),
                reportRepository = mockReportRepo)

        val results = sut.getAllReportVersions()

        assertThat(results.count()).isEqualTo(2)

        assertThat(results[0].name).isEqualTo("test")
        assertThat(results[0].id).isEqualTo("v1")
        assertThat(results[0].customFields.keys.count()).isEqualTo(2)
        assertThat(results[0].customFields["author"]).isEqualTo("author authorson")
        assertThat(results[0].customFields["requester"]).isEqualTo(null)
        assertThat(results[0].parameterValues.keys.count()).isEqualTo(1)
        assertThat(results[0].parameterValues["p1"]).isEqualTo("param1")

        assertThat(results[1].name).isEqualTo("test")
        assertThat(results[1].id).isEqualTo("v2")
        assertThat(results[1].customFields.keys.count()).isEqualTo(2)
        assertThat(results[1].customFields["author"]).isEqualTo(null)
        assertThat(results[1].customFields["requester"]).isEqualTo(null)
        assertThat(results[1].parameterValues.keys.count()).isEqualTo(1)
        assertThat(results[1].parameterValues["p2"]).isEqualTo("param2")
    }

    @Test
    fun `can getLatestChangelogByName`()
    {
        val sut = createSut()
        val result = sut.getLatestChangelogByName("test")
        assertThat(result.count()).isEqualTo(1)
        assertThat(result[0])
                .isEqualToComparingFieldByField(Changelog("v1", "public", "getLatestChangelog", true, true))
    }

    @Test
    fun `can getChangelogByNameAndVersion`()
    {
        val sut = createSut()
        val result = sut.getChangelogByNameAndVersion("test", "v1")
        assertThat(result.count()).isEqualTo(1)
        assertThat(result[0])
                .isEqualToComparingFieldByField(Changelog("v1", "public", "getByNameAndVersion", true, true))
    }

}
