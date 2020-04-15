package org.vaccineimpact.orderlyweb.tests.database_tests.ReportRepositoryTests

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.Test
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.db.repositories.OrderlyReportRepository
import org.vaccineimpact.orderlyweb.db.repositories.ReportRepository
import org.vaccineimpact.orderlyweb.errors.UnknownObjectError
import org.vaccineimpact.orderlyweb.test_helpers.CleanDatabaseTests
import org.vaccineimpact.orderlyweb.test_helpers.insertReport
import org.vaccineimpact.orderlyweb.test_helpers.insertReportWithCustomFields

class VersionTests : CleanDatabaseTests()
{

    private fun createSut(isReviewer: Boolean = false): ReportRepository
    {
        return OrderlyReportRepository(isReviewer, true, listOf())
    }

    @Test
    fun `getAllReportVersions returns report names user is authorized to see`()
    {
        insertReport("goodname", "va")
        insertReport("badname", "vb")

        val mockContext = mock<ActionContext> {
            on { it.reportReadingScopes } doReturn listOf("goodname")
        }

        val sut = OrderlyReportRepository(mockContext)

        val result = sut.getAllReportVersions()
        Assertions.assertThat(result).hasSize(1)
        Assertions.assertThat(result[0].name).isEqualTo("goodname")
    }

    @Test
    fun `getAllReportVersions returns all report names if user has global read permissions`()
    {
        insertReport("goodname", "va")
        insertReport("anothername", "vb")

        val mockContext = mock<ActionContext> {
            on { it.isGlobalReader() } doReturn true
        }

        val sut = OrderlyReportRepository(mockContext)

        val results = sut.getAllReportVersions()
        Assertions.assertThat(results.count()).isEqualTo(2)
    }

    @Test
    fun `getReportVersion throws unknown object error if report version not published`()
    {
        insertReport("test", "version1", published = false)

        val sut = createSut()
        Assertions.assertThatThrownBy { sut.getReportVersion("test", "version1") }
                .isInstanceOf(UnknownObjectError::class.java)
    }

    @Test
    fun `getReportVersion throws unknown object error if report version doesnt exist`()
    {
        insertReport("test", "version1")

        val sut = createSut()

        Assertions.assertThatThrownBy { sut.getReportVersion("test", "dsajkdsj") }
                .isInstanceOf(UnknownObjectError::class.java)
    }


    @Test
    fun `getReportVersion throws unknown object error if report name not found`()
    {
        insertReport("test", "version1")

        val sut = createSut()

        Assertions.assertThatThrownBy { sut.getReportVersion("dsajkdsj", "version") }
                .isInstanceOf(UnknownObjectError::class.java)
    }

    @Test
    fun `reviewer can get unpublished version details`()
    {
        insertReport("test", "version1", published = false)

        val sut = createSut(isReviewer = true)

        val result = sut.getReportVersion("test", "version1")

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

        val sut = createSut()

        val results = sut.getAllReportVersions()

        Assertions.assertThat(results.count()).isEqualTo(6)

        Assertions.assertThat(results[0].name).isEqualTo("test")
        Assertions.assertThat(results[0].displayName).isEqualTo("display name test")
        Assertions.assertThat(results[0].latestVersion).isEqualTo("vz")
        Assertions.assertThat(results[0].id).isEqualTo("va")
        Assertions.assertThat(results[0].published).isTrue()

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

        val sut = createSut(isReviewer = true)

        val results = sut.getAllReportVersions()

        Assertions.assertThat(results.count()).isEqualTo(7)

        Assertions.assertThat(results[0].name).isEqualTo("test")
        Assertions.assertThat(results[0].displayName).isEqualTo("display name test")
        Assertions.assertThat(results[0].latestVersion).isEqualTo("vz")
        Assertions.assertThat(results[0].id).isEqualTo("va")
        Assertions.assertThat(results[0].published).isTrue()

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
    fun `can get all custom fields`()
    {
        val sut = createSut()
        val result = sut.getAllCustomFields()
        Assertions.assertThat(result.keys).containsExactly("author", "requester")
        Assertions.assertThat(result["author"]).isEqualTo(null)
        Assertions.assertThat(result["requester"]).isEqualTo(null)
    }

    @Test
    fun `can get custom fields for versions`()
    {
        insertReportWithCustomFields("test1", "v1", mapOf("author" to "authorer"))
        insertReportWithCustomFields("test2", "v2", mapOf())
        insertReportWithCustomFields("test2", "v3", mapOf("requester" to "requester mcfunderface"))

        val sut = createSut()
        val result = sut.getCustomFieldsForVersions(listOf("v1", "v2", "v3"))

        Assertions.assertThat(result.keys).containsExactly("v1", "v3")
        Assertions.assertThat(result["v1"]!!.keys).containsExactly("author")
        Assertions.assertThat(result["v1"]!!["author"]).isEqualTo("authorer")
        Assertions.assertThat(result["v3"]!!.keys).containsExactly("requester")
        Assertions.assertThat(result["v3"]!!["requester"]).isEqualTo("requester mcfunderface")
    }
}
