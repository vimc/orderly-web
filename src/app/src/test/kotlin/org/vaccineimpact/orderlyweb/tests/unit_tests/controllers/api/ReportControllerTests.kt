package org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.api

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.api.ReportController
import org.vaccineimpact.orderlyweb.db.Config
import org.vaccineimpact.orderlyweb.db.OrderlyClient
import org.vaccineimpact.orderlyweb.db.repositories.ReportRepository
import org.vaccineimpact.orderlyweb.errors.MissingRequiredPermissionError
import org.vaccineimpact.orderlyweb.models.ReportVersionWithDescLatest
import org.vaccineimpact.orderlyweb.models.Changelog
import org.vaccineimpact.orderlyweb.models.Report
import org.vaccineimpact.orderlyweb.models.ReportVersionWithDescCustomFieldsLatestParamsTags
import org.vaccineimpact.orderlyweb.models.permissions.PermissionSet
import java.time.Instant

class ReportControllerTests : ControllerTest()
{
    private val mockConfig = mock<Config> {
        on { this.get("orderly.root") } doReturn "root/"
        on { this.authorizationEnabled } doReturn true
    }

    private val reportName = "report1"
    private val reportKey = "123"

    private val reports = listOf(Report(reportName, "test full name 1", "v1"),
            Report("testname2", "test full name 2", "v1"))


    private val reportVersions = listOf(
            ReportVersionWithDescCustomFieldsLatestParamsTags(ReportVersionWithDescLatest(reportName, "display1", "v1", true, Instant.now(), "v1", "desc"),
                    mapOf("author" to "auth", "requester" to "req"), mapOf("p1" to "v1"), listOf("t1", "t2")),
            ReportVersionWithDescCustomFieldsLatestParamsTags(ReportVersionWithDescLatest("r2", "display2", "v2", true, Instant.now(), "v1", "v2"),
                    mapOf("author" to "auth", "requester" to "req"), mapOf("p1" to "v1", "p2" to "v2"), listOf("t1"))
    )

    private val mockOrderly = mock<OrderlyClient> {
        on { this.getAllReportVersions() } doReturn reportVersions
    }

    private val mockReportRepo = mock<ReportRepository> {
        on { this.getAllReports() } doReturn reports
    }

    @Test
    fun `getAllReports throws MissingRequiredPermission error if user has no report reading permissions`()
    {
        val mockContext = mock<ActionContext> {
            on { it.permissions } doReturn PermissionSet()
        }

        val sut = ReportController(mockContext, mockOrderly, mockReportRepo, mockConfig)

        assertThatThrownBy { sut.getAllReports() }
                .isInstanceOf(MissingRequiredPermissionError::class.java)
                .hasMessageContaining("*/reports.read")
    }

    @Test
    fun `getAllVersions throws MissingRequiredPermission error if user has no report reading permissions`()
    {
        val mockContext = mock<ActionContext> {
            on { it.permissions } doReturn PermissionSet()
        }

        val sut = ReportController(mockContext, mock(), mock(), mockConfig)

        assertThatThrownBy { sut.getAllVersions() }
                .isInstanceOf(MissingRequiredPermissionError::class.java)
                .hasMessageContaining("*/reports.read")
    }

    @Test
    fun `getByName returns all reports versions by name`()
    {
        val reportVersions = listOf("version1", "version2")

        val mockReportRepo = mock<ReportRepository> {
            on { this.getReportsByName(reportName) } doReturn reportVersions
        }

        val mockContext = mock<ActionContext> {
            on { this.permissions } doReturn PermissionSet()
            on { it.params(":name") } doReturn reportName
        }

        val sut = ReportController(mockContext, mock(), mockReportRepo, mockConfig)

        assertThat(sut.getVersionsByName()).isEqualTo(reportVersions)
    }


    @Test
    fun `getLatestChangelogByName returns changelog`()
    {
        val reportName = "reportName"

        val latestVersion = "latestVersion"
        val changelogs = listOf(Changelog(latestVersion, "public", "did a thing", true, true),
                Changelog(latestVersion, "public", "did another thing", true, true))

        val orderly = mock<OrderlyClient> {
            on { this.getLatestChangelogByName(reportName) } doReturn changelogs
        }

        val mockContext = mock<ActionContext> {
            on { this.permissions } doReturn PermissionSet()
            on { this.params(":name") } doReturn reportName
        }

        val sut = ReportController(mockContext, orderly, mockReportRepo, mockConfig)

        val result = sut.getLatestChangelogByName()
        assertThat(result.count()).isEqualTo(changelogs.count())
        for (i in 0 until result.count() - 1)
        {
            assertThat(result[i]).isEqualTo(changelogs[i])
        }
    }

    @Test
    fun `publishes report`()
    {
        val name = "reportName"
        val version = "v1"
        val mockReportRepo = mock<ReportRepository>() {
            on { setPublishStatus(name, version) } doReturn false
        }

        val mockContext = mock<ActionContext> {
            on { this.params(":version") } doReturn version
            on { this.params(":name") } doReturn name
            on { this.queryParams("value") } doReturn "false"
        }

        val sut = ReportController(mockContext, mock(), mockReportRepo, mockConfig)

        val result = sut.publish()

        assertThat(result).isEqualTo(false)

        verify(mockReportRepo).setPublishStatus(name, version, false)
    }

    @Test
    fun `publishes report with default value when query param is blank`()
    {
        val name = "reportName"
        val version = "v1"
        val mockReportRepo = mock<ReportRepository>() {
            on { setPublishStatus(name, version) } doReturn true
        }

        val mockContext = mock<ActionContext> {
            on { this.params(":version") } doReturn version
            on { this.params(":name") } doReturn name
            on { this.queryParams("value") } doReturn ""
        }

        val sut = ReportController(mockContext, mock(), mockReportRepo, mockConfig)

        val result = sut.publish()

        assertThat(result).isEqualTo(true)

        verify(mockReportRepo).setPublishStatus(name, version, true)
    }

    @Test
    fun `publishes report with default value when query param is not provided`()
    {
        val name = "reportName"
        val version = "v1"
        val mockReportRepo = mock<ReportRepository>() {
            on { setPublishStatus(name, version) } doReturn true
        }

        val mockContext = mock<ActionContext> {
            on { this.params(":version") } doReturn version
            on { this.params(":name") } doReturn name
        }

        val sut = ReportController(mockContext, mock(), mockReportRepo, mockConfig)

        val result = sut.publish()

        assertThat(result).isEqualTo(true)

        verify(mockReportRepo).setPublishStatus(name, version, true)
    }

}
