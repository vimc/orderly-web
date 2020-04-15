package org.vaccineimpact.orderlyweb.tests.database_tests

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.orderlyweb.db.Orderly
import org.vaccineimpact.orderlyweb.db.OrderlyClient
import org.vaccineimpact.orderlyweb.db.repositories.ReportRepository
import org.vaccineimpact.orderlyweb.errors.UnknownObjectError
import org.vaccineimpact.orderlyweb.models.*
import org.vaccineimpact.orderlyweb.test_helpers.*
import org.vaccineimpact.orderlyweb.tests.insertArtefact
import org.vaccineimpact.orderlyweb.tests.insertData
import org.vaccineimpact.orderlyweb.tests.insertFileInput
import java.time.Instant

class VersionTests : CleanDatabaseTests()
{
    val now = Instant.now()

    private val basicReportVersion =
            BasicReportVersion(
                    "test",
                    "display name",
                    "v1",
                    true,
                    now,
                    "vz",
                    "descripion")

    private val mockReportRepo = mock<ReportRepository> {
        on { getReportVersion("test", "v1") } doReturn basicReportVersion
        on { getAllReportVersions() } doReturn listOf(basicReportVersion, basicReportVersion.copy(id = "v2"))
        on { getParametersForVersions(listOf("v1")) } doReturn mapOf("v1" to mapOf("p1" to "param1", "p2" to "param2"))
    }

    private fun createSut(isReviewer: Boolean = false): OrderlyClient
    {
        return Orderly(isReviewer, true, listOf(), reportRepository = mockReportRepo)
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
        insertReport("test", "v1")
        insertReportTags("test", "r1", "r2")
        insertVersionTags("v1", "v2", "v1")
        insertOrderlyTags("v1", "o1")

        // don't mock the repos as this method still goes directly to the db
        val sut = Orderly(true, true, listOf())
        val result = sut.getReportVersionTags("test", "v1")
        assertThat(result.versionTags).containsExactlyElementsOf(listOf("v1", "v2"))
        assertThat(result.reportTags).containsExactlyElementsOf(listOf("r1", "r2"))
        assertThat(result.orderlyTags).containsExactlyElementsOf(listOf("o1"))
    }

    @Test
    fun `getTags throws unknown object error if report version does not exist`()
    {
        // don't mock the repos as this method still goes directly to the db
        val sut = Orderly(true, true, listOf())
        Assertions.assertThatThrownBy { sut.getReportVersionTags("nonexistent", "version1") }
                .isInstanceOf(UnknownObjectError::class.java)
    }

    @Test
    fun `getTags throws unknown object error for reader if version is not published`()
    {
        insertReport("test", "version1", published = false)

        // don't mock the repos as this method still goes directly to the db
        val sut = Orderly(isReviewer = false, isGlobalReader = true, reportReadingScopes = listOf())
        Assertions.assertThatThrownBy { sut.getReportVersionTags("test", "version1") }
                .isInstanceOf(UnknownObjectError::class.java)
    }

    @Test
    fun `getAllReportVersions returns version tags`()
    {
        insertReport("report", "v1")
        insertVersionTags("v1", "c-tag", "a-tag", "b-tag")

        insertReport("report", "v2")
        insertVersionTags("v2", "aa-tag")

        insertReport("report", "v3")

        // don't mock the repos as this method goes directly to the db for tags
        val sut = Orderly(isReviewer = true, isGlobalReader = true, reportReadingScopes = listOf())
        val results = sut.getAllReportVersions()

        assertThat(results[0].id).isEqualTo("v1")
        //tags should be sorted
        assertThat(results[0].tags).containsExactlyElementsOf(listOf("a-tag", "b-tag", "c-tag"))

        assertThat(results[1].id).isEqualTo("v2")
        assertThat(results[1].tags).containsExactlyElementsOf(listOf("aa-tag"))

        assertThat(results[2].id).isEqualTo("v3")
        assertThat(results[2].tags.count()).isEqualTo(0)
    }

    @Test
    fun `getAllReportVersions includes report tags`()
    {
        insertReport("report", "v1")
        insertReportTags("report", "d-tag", "b-tag")
        insertVersionTags("v1", "c-tag", "a-tag", "b-tag")

        insertReport("report2", "v2")
        insertVersionTags("v2", "aa-tag")

        insertReport("report3", "v3")
        insertReportTags("report3", "a-tag")

        // don't mock the repos as this method goes directly to the db for tags
        val sut = Orderly(isReviewer = true, isGlobalReader = true, reportReadingScopes = listOf())
        val results = sut.getAllReportVersions()

        assertThat(results[0].id).isEqualTo("v1")
        assertThat(results[0].tags).containsExactlyElementsOf(listOf("a-tag", "b-tag", "c-tag", "d-tag"))

        assertThat(results[1].id).isEqualTo("v2")
        assertThat(results[1].tags).containsExactlyElementsOf(listOf("aa-tag"))

        assertThat(results[2].id).isEqualTo("v3")
        assertThat(results[2].tags).containsExactlyElementsOf(listOf("a-tag"))
    }

    @Test
    fun `getAllReportVersions includes orderly tags`()
    {
        insertReport("report", "v1")
        insertVersionTags("v1", "a", "c")
        insertOrderlyTags("v1", "b", "d")

        insertReport("report2", "v2")
        insertReportTags("report2", "e")
        insertOrderlyTags("v2", "f", "e")

        insertReport("report3", "v3")
        insertOrderlyTags("v3", "g")

        // don't mock the repos as this method goes directly to the db for tags
        val sut = Orderly(isReviewer = true, isGlobalReader = true, reportReadingScopes = listOf())
        val results = sut.getAllReportVersions()

        assertThat(results[0].id).isEqualTo("v1")
        assertThat(results[0].tags).containsExactlyElementsOf(listOf("a", "b", "c", "d"))

        assertThat(results[1].id).isEqualTo("v2")
        assertThat(results[1].tags).containsExactlyElementsOf(listOf("e", "f"))

        assertThat(results[2].id).isEqualTo("v3")
        assertThat(results[2].tags).containsExactlyElementsOf(listOf("g"))
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

}
