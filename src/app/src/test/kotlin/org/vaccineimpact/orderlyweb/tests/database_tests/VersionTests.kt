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
import org.vaccineimpact.orderlyweb.models.BasicReportVersion
import org.vaccineimpact.orderlyweb.test_helpers.*
import java.sql.Timestamp
import java.time.Instant

class VersionTests : CleanDatabaseTests()
{
    val name = "r1"
    val version = "v1"

    private val mockReportRepo = mock<ReportRepository>() {
        on { getReportVersion(name, version) } doReturn
                BasicReportVersion(name, "display name", version, true, Instant.now(), "v", "desc")
        on { getAllCustomFields() } doReturn
                mapOf("author" to null, "requester" to null)
        on { getCustomFieldsForVersions(listOf(version)) } doReturn
                mapOf(version to mapOf("author" to "test2 author"))
        on { getAllReportVersions() } doReturn
                listOf(BasicReportVersion(name, "display name", version, true, Instant.now(), "v", "desc"))
    }

    private fun createSut(isReviewer: Boolean = false): OrderlyClient
    {
        return Orderly(isReviewer, true, listOf(), mockReportRepo)
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
    fun `can get all report versions`()
    {
        val sut = createSut()

        val results = sut.getAllReportVersions()

        assertThat(results.count()).isEqualTo(1)

        assertThat(results[0].name).isEqualTo(name)
        assertThat(results[0].displayName).isEqualTo("display name")
        assertThat(results[0].latestVersion).isEqualTo("v")
        assertThat(results[0].id).isEqualTo(version)
        assertThat(results[0].published).isTrue()
        assertThat(results[0].customFields.keys.count()).isEqualTo(2)
        assertThat(results[0].customFields["author"]).isEqualTo("author authorson")
        assertThat(results[0].customFields["requester"]).isEqualTo(null)
    }


}