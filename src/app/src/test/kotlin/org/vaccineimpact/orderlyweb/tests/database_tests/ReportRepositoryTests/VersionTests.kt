package org.vaccineimpact.orderlyweb.tests.database_tests.ReportRepositoryTests

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.db.repositories.OrderlyReportRepository
import org.vaccineimpact.orderlyweb.db.repositories.ReportRepository
import org.vaccineimpact.orderlyweb.errors.UnknownObjectError
import org.vaccineimpact.orderlyweb.test_helpers.*
import org.vaccineimpact.orderlyweb.tests.insertUser
import org.vaccineimpact.orderlyweb.db.Tables
import org.vaccineimpact.orderlyweb.db.JooqContext

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
        assertThat(result).hasSize(1)
        assertThat(result[0].name).isEqualTo("goodname")
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
        assertThat(results.count()).isEqualTo(2)
    }

    @Test
    fun `getReportVersion throws unknown object error if report version not published`()
    {
        insertReport("test", "version1", published = false)

        val sut = createSut()
        assertThatThrownBy { sut.getReportVersion("test", "version1") }
                .isInstanceOf(UnknownObjectError::class.java)
    }

    @Test
    fun `getReportVersion throws unknown object error if report version doesnt exist`()
    {
        insertReport("test", "version1")

        val sut = createSut()

        assertThatThrownBy { sut.getReportVersion("test", "dsajkdsj") }
                .isInstanceOf(UnknownObjectError::class.java)
    }


    @Test
    fun `getReportVersion throws unknown object error if report name not found`()
    {
        insertReport("test", "version1")

        val sut = createSut()

        assertThatThrownBy { sut.getReportVersion("dsajkdsj", "version") }
                .isInstanceOf(UnknownObjectError::class.java)
    }

    @Test
    fun `reviewer can get version details for report with no publish record`()
    {
        insertReport("test", "version1", elapsed=4.3, gitBranch="master", gitCommit="abc123",
                addOrderlyWebReportVersion = false)

        val sut = createSut(isReviewer = true)

        val result = sut.getReportVersion("test", "version1")

        assertThat(result.name).isEqualTo("test")
        assertThat(result.id).isEqualTo("version1")
        assertThat(result.published).isFalse()
        assertThat(result.elapsed).isEqualTo(4.3)
        assertThat(result.gitBranch).isEqualTo("master")
        assertThat(result.gitCommit).isEqualTo("abc123")
    }

    @Test
    fun `reviewer can get unpublished version details`()
    {
        insertReport("test", "version1", published = false, elapsed=4.3,  gitBranch="master", gitCommit="abc123")

        val sut = createSut(isReviewer = true)

        val result = sut.getReportVersion("test", "version1")

        assertThat(result.name).isEqualTo("test")
        assertThat(result.id).isEqualTo("version1")
        assertThat(result.published).isFalse()
        assertThat(result.elapsed).isEqualTo(4.3)
        assertThat(result.gitBranch).isEqualTo("master")
        assertThat(result.gitCommit).isEqualTo("abc123")
    }

    @Test
    fun `version details can include git branch from report run table`()
    {
        insertReport("test", "version1", published = false, gitBranch=null, gitCommit="abc123")
        insertUser("test.user@example.com", "user.name")
        insertReportRun("weird_gazelle", "test.user@example.com", "test", gitBranch="master", reportVersion="version1")
        val sut = createSut(isReviewer = true)
        val result = sut.getReportVersion("test", "version1")
        assertThat(result.name).isEqualTo("test")
        assertThat(result.id).isEqualTo("version1")
        assertThat(result.published).isFalse()
        assertThat(result.gitBranch).isEqualTo("master")
        assertThat(result.gitCommit).isEqualTo("abc123")
    }

    @Test
    fun `reader can get all published report versions`()
    {
        insertReport("test", "va", display = "display name a")
        insertReport("test", "vz", display = "display name z")
        insertReport("test2", "vc")
        insertReport("test2", "vb")
        insertReport("test2", "vd")
        insertReport("test3", "test3version")
        insertReport("test3", "test3versionunpublished", published = false)

        val sut = createSut()

        val results = sut.getAllReportVersions()

        assertThat(results.count()).isEqualTo(6)

        assertThat(results[0].name).isEqualTo("test")
        assertThat(results[0].displayName).isEqualTo("display name a")
        assertThat(results[0].latestVersion).isEqualTo("vz")
        assertThat(results[0].id).isEqualTo("va")
        assertThat(results[0].published).isTrue()

        assertThat(results[1].name).isEqualTo("test")
        assertThat(results[1].displayName).isEqualTo("display name z")
        assertThat(results[1].id).isEqualTo("vz")
        assertThat(results[1].latestVersion).isEqualTo("vz")

        assertThat(results[2].name).isEqualTo("test2")
        assertThat(results[2].id).isEqualTo("vb")
        assertThat(results[2].latestVersion).isEqualTo("vd")

        assertThat(results[3].name).isEqualTo("test2")
        assertThat(results[3].id).isEqualTo("vc")
        assertThat(results[3].latestVersion).isEqualTo("vd")

        assertThat(results[4].name).isEqualTo("test2")
        assertThat(results[4].id).isEqualTo("vd")
        assertThat(results[4].latestVersion).isEqualTo("vd")

        assertThat(results[5].name).isEqualTo("test3")
        assertThat(results[5].id).isEqualTo("test3version")
        assertThat(results[5].latestVersion).isEqualTo("test3version")
    }

    @Test
    fun `reviewer can get all published and unpublished report versions`()
    {
        insertReport("test", "va", display = "display name a")
        insertReport("test", "vz")
        insertReport("test2", "vc")
        insertReport("test2", "vb")
        insertReport("test2", "vd")
        insertReport("test3", "test3version")
        insertReport("test3", "test3versionunpublished", published = false)

        val sut = createSut(isReviewer = true)

        val results = sut.getAllReportVersions()

        assertThat(results.count()).isEqualTo(7)

        assertThat(results[0].name).isEqualTo("test")
        assertThat(results[0].displayName).isEqualTo("display name a")
        assertThat(results[0].latestVersion).isEqualTo("vz")
        assertThat(results[0].id).isEqualTo("va")
        assertThat(results[0].published).isTrue()

        assertThat(results[1].name).isEqualTo("test")
        assertThat(results[1].id).isEqualTo("vz")
        assertThat(results[1].latestVersion).isEqualTo("vz")

        assertThat(results[2].name).isEqualTo("test2")
        assertThat(results[2].id).isEqualTo("vb")
        assertThat(results[2].latestVersion).isEqualTo("vd")

        assertThat(results[3].name).isEqualTo("test2")
        assertThat(results[3].id).isEqualTo("vc")
        assertThat(results[3].latestVersion).isEqualTo("vd")

        assertThat(results[4].name).isEqualTo("test2")
        assertThat(results[4].id).isEqualTo("vd")
        assertThat(results[4].latestVersion).isEqualTo("vd")

        assertThat(results[5].name).isEqualTo("test3")
        assertThat(results[5].id).isEqualTo("test3version")
        assertThat(results[5].latestVersion).isEqualTo("test3versionunpublished")

        assertThat(results[6].name).isEqualTo("test3")
        assertThat(results[6].id).isEqualTo("test3versionunpublished")
        assertThat(results[6].latestVersion).isEqualTo("test3versionunpublished")
    }

    @Test
    fun `can get all custom fields`()
    {
        val sut = createSut()
        val result = sut.getAllCustomFields()
        assertThat(result.keys).containsExactly("author", "requester")
        assertThat(result["author"]).isEqualTo(null)
        assertThat(result["requester"]).isEqualTo(null)
    }

    @Test
    fun `can get custom fields for versions`()
    {
        insertReportWithCustomFields("test1", "v1", mapOf("author" to "authorer"))
        insertReportWithCustomFields("test2", "v2", mapOf())
        insertReportWithCustomFields("test2", "v3", mapOf("requester" to "requester mcfunderface"))

        val sut = createSut()
        val result = sut.getCustomFieldsForVersions(listOf("v1", "v2", "v3"))

        assertThat(result.keys).containsExactly("v1", "v3")
        assertThat(result["v1"]!!.keys).containsExactly("author")
        assertThat(result["v1"]!!["author"]).isEqualTo("authorer")
        assertThat(result["v3"]!!.keys).containsExactly("requester")
        assertThat(result["v3"]!!["requester"]).isEqualTo("requester mcfunderface")
    }

    @Test
    fun `can get parameters for report versions`()
    {
        insertReport("test", "va")
        insertVersionParameterValues("va", mapOf("p1" to "param1", "p2" to "param2"))
        insertReport("test", "vz")
        insertVersionParameterValues("vz", mapOf("p1" to "param3"))

        insertReport("test2", "vc")
        insertReport("test2", "vb")

        val sut = createSut()

        val results = sut.getParametersForVersions(listOf("va", "vz", "vb", "vc"))

        assertThat(results.keys).containsExactly("va", "vz")

        assertThat(results["va"]!!.keys.count()).isEqualTo(2)
        assertThat(results["va"]!!["p1"]).isEqualTo("param1")
        assertThat(results["va"]!!["p2"]).isEqualTo("param2")

        assertThat(results["vz"]!!.keys.count()).isEqualTo(1)
        assertThat(results["vz"]!!["p1"]).isEqualTo("param3")
    }

    @Test
    fun `can get latest version for reviewer`()
    {
        insertReport("test", "version1")
        insertReport("test", "version2", published = false)
        insertReport("anotherreport", "v1")

        val sut = createSut(true)
        val result = sut.getLatestVersion("test")

        assertThat(result.id).isEqualTo("version2")
    }

    @Test
    fun `can get latest published version for reader`()
    {
        insertReport("test", "version1")
        insertReport("test", "version2", published = false)
        insertReport("anotherreport", "v1")

        val sut = createSut(false)
        val result = sut.getLatestVersion("test")

        assertThat(result.id).isEqualTo("version1")
    }

    @Test
    fun `getLatestVersion throws unknown object error for reviewer if no versions`()
    {
        insertReport("anotherreport", "v1")

        val sut = createSut(true)
        assertThatThrownBy {
            sut.getLatestVersion("test")
        }.isInstanceOf(UnknownObjectError::class.java)
    }

    @Test
    fun `getLatestVersion throws unknown object error for reader if no published versions`()
    {
        insertReport("test", "version1", published = false)
        insertReport("anotherreport", "v1")

        val sut = createSut(false)
        assertThatThrownBy {
            sut.getLatestVersion("test")
        }.isInstanceOf(UnknownObjectError::class.java)
    }

    @Test
    fun `can publish multiple versions at once`()
    {
        insertReport("test", "v1", published = false)
        insertReport("test", "v2", published = false)
        insertReport("anotherreport", "b1", published = false)

        val sut = createSut(true)
        sut.publish(listOf("v1", "b1"))

        assertThat(sut.getReportVersion("test", "v1").published).isTrue()
        assertThat(sut.getReportVersion("test", "v2").published).isFalse()
        assertThat(sut.getReportVersion("anotherreport", "b1").published).isTrue()
    }

    @Test
    fun `can get instances for report version`()
    {
        insertReport("test1", "version1")
        insertReport("test2", "version2")

        JooqContext().use {
            val instanceRecord = it.dsl.newRecord(Tables.REPORT_VERSION_INSTANCE)
                    .apply {
                        this.id = 1
                        this.reportVersion = "version1"
                        this.instance = "instance1"
                        this.type = "type1"
                    }
            instanceRecord.store()
            
            val instanceRecord2 = it.dsl.newRecord(Tables.REPORT_VERSION_INSTANCE)
                    .apply {
                        this.id = 2
                        this.reportVersion = "version1"
                        this.instance = "instance2"
                        this.type = "type2"
                    }
            instanceRecord2.store()
        }

        val sut = createSut()

        val results = sut.getReportVersionInstances("version1")
        assertThat(results).isEqualTo(mapOf("type1" to "instance1", "type2" to "instance2"))
    }

    @Test
    fun `get instances returns empty map if none exist`()
    {
        insertReport("test1", "version1")

        val sut = createSut(true)
        val map = emptyMap<String, String>()

        val results = sut.getReportVersionInstances("version1")
        assertThat(results).isEqualTo(map)
    }

}
