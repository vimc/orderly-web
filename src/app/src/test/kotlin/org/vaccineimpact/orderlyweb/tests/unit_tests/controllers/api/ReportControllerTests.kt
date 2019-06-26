package org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.api

import com.nhaarman.mockito_kotlin.*
import khttp.responses.Response
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.vaccineimpact.orderlyweb.models.Changelog
import org.vaccineimpact.orderlyweb.models.Report
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.PermissionSet
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.OrderlyServerAPI
import org.vaccineimpact.orderlyweb.ZipClient
import org.vaccineimpact.orderlyweb.controllers.api.ReportController
import org.vaccineimpact.orderlyweb.db.Config
import org.vaccineimpact.orderlyweb.db.OrderlyClient
import org.vaccineimpact.orderlyweb.errors.MissingRequiredPermissionError
import org.vaccineimpact.orderlyweb.models.ReportVersion
import java.time.Instant

class ReportControllerTests : ControllerTest()
{
    private val mockConfig = mock<Config> {
        on { this.get("orderly.root") } doReturn "root/"
        on { this.authorizationEnabled } doReturn true
    }

    private val reportName = "report1"

    private val permissionSetForSingleReport = PermissionSet(
            setOf(ReifiedPermission("reports.read", Scope.Specific("report", reportName))))

    private val permissionSetGlobal = PermissionSet(
            setOf(ReifiedPermission("reports.read", Scope.Global())))

    private val reports = listOf(Report(reportName, "test full name 1", "v1"),
            Report("testname2", "test full name 2", "v1"))


    private val reportVersions = listOf(
            ReportVersion(reportName, "display1", "v1", "v1", true, Instant.now(),
                    "auth", "req"),
            ReportVersion("r2", "display2", "v2", "v2", true, Instant.now(),
                    "auth", "req")
    )

    private val mockOrderly = mock<OrderlyClient> {
        on { this.getAllReports() } doReturn reports
        on { this.getAllReportVersions() } doReturn reportVersions
    }

    @Test
    fun `runs a report`()
    {
        val actionContext = mock<ActionContext> {
            on { this.params(":name") } doReturn reportName
            on { this.permissions } doReturn PermissionSet()
        }

        val mockAPIResponse = mock<Response>() {
            on { this.text } doReturn "okayresponse"
        }

        val apiClient = mock<OrderlyServerAPI>() {
            on { this.post(any(), any()) } doReturn mockAPIResponse
        }

        val sut = ReportController(actionContext, mock<OrderlyClient>(),
                mock<ZipClient>(), apiClient, mockConfig)

        val result = sut.run()

        assertThat(result).isEqualTo("okayresponse")
    }

    @Test
    fun `getAllReports returns report names user is authorized to see`()
    {
        val mockContext = mock<ActionContext> {
            on { it.permissions } doReturn permissionSetForSingleReport
        }

        val sut = ReportController(mockContext, mockOrderly, mock(),
                mock(),
                mockConfig)

        val result = sut.getAllReports()
        assertThat(result).hasSize(1)
        assertThat(result[0].name).isEqualTo(reportName)
    }

    @Test
    fun `getAllReports returns all report names if fine grained auth is turned off`()
    {
        val mockContext = mock<ActionContext> {
            on { it.permissions } doReturn permissionSetForSingleReport
        }

        val mockConfig = mock<Config> {
            on { authorizationEnabled } doReturn false
        }

        val sut = ReportController(mockContext, mockOrderly, mock(),
                mock(),
                mockConfig)

        val result = sut.getAllReports()
        assertThat(result).hasSize(2)
    }

    @Test
    fun `getAllReports returns all report names if user has global read permissions`()
    {
        val mockContext = mock<ActionContext> {
            on { it.permissions } doReturn permissionSetGlobal
        }

        val sut = ReportController(mockContext, mockOrderly, mock<ZipClient>(),
                mock<OrderlyServerAPI>(),
                mockConfig)

        val result = sut.getAllReports()
        assertThat(result).hasSameElementsAs(reports)
    }


    @Test
    fun `getAllReports throws MissingRequiredPermission error if user has no report reading permissions`()
    {
        val mockContext = mock<ActionContext> {
            on { it.permissions } doReturn PermissionSet()
        }

        val sut = ReportController(mockContext, mockOrderly, mock(),
                mock(),
                mockConfig)

        assertThatThrownBy { sut.getAllReports() }
                .isInstanceOf(MissingRequiredPermissionError::class.java)
                .hasMessageContaining("*/reports.read")
    }


    @Test
    fun `getAllVersions returns all report versions if user has global read permissions`()
    {
        val mockContext = mock<ActionContext> {
            on { it.permissions } doReturn permissionSetGlobal
        }

        val sut = ReportController(mockContext, mockOrderly, mock<ZipClient>(),
                mock<OrderlyServerAPI>(),
                mockConfig)

        val result = sut.getAllVersions()
        assertThat(result).hasSameElementsAs(reportVersions)
    }

    @Test
    fun `getAllVersions returns only versions user can see`()
    {
        val mockContext = mock<ActionContext> {
            on { it.permissions } doReturn permissionSetForSingleReport
        }

        val sut = ReportController(mockContext, mockOrderly, mock(),
                mock(),
                mockConfig)

        val result = sut.getAllVersions()
        assertThat(result).hasSize(1)
        assertThat(result[0].name).isEqualTo(reportName)
    }

    @Test
    fun `getAllVersions returns all versions if fine grained auth is turned off`()
    {
        val mockContext = mock<ActionContext> {
            on { it.permissions } doReturn permissionSetForSingleReport
        }

        val mockConfig = mock<Config> {
            on { authorizationEnabled } doReturn false
        }

        val sut = ReportController(mockContext, mockOrderly, mock(),
                mock(),
                mockConfig)

        val result = sut.getAllVersions()
        assertThat(result).hasSize(2)
    }

    @Test
    fun `getAllVersions throws MissingRequiredPermission error if user has no report reading permissions`()
    {
        val mockContext = mock<ActionContext> {
            on { it.permissions } doReturn PermissionSet()
        }

        val sut = ReportController(mockContext, mock(), mock(),
                mock(), mockConfig)

        assertThatThrownBy { sut.getAllVersions() }
                .isInstanceOf(MissingRequiredPermissionError::class.java)
                .hasMessageContaining("*/reports.read")
    }

    @Test
    fun `getByName returns all reports versions by name`()
    {
        val reportVersions = listOf("version1", "version2")

        val orderly = mock<OrderlyClient> {
            on { this.getReportsByName(reportName) } doReturn reportVersions
        }

        val mockContext = mock<ActionContext> {
            on { this.permissions } doReturn PermissionSet()
            on { it.params(":name") } doReturn reportName
        }

        val sut = ReportController(mockContext, orderly, mock<ZipClient>(),
                mock<OrderlyServerAPI>(),
                mockConfig)

        assertThat(sut.getVersionsByName()).isEqualTo(reportVersions)
    }


    @Test
    fun `getLatestChangelogByName returns changelog`()
    {
        val reportName = "reportName"

        val latestVersion = "latestVersion"
        val changelogs = listOf(Changelog(latestVersion, "public", "did a thing", true),
                Changelog(latestVersion, "public", "did another thing", true))

        val orderly = mock<OrderlyClient> {
            on { this.getLatestChangelogByName(reportName) } doReturn changelogs
        }

        val mockContext = mock<ActionContext> {
            on { this.permissions } doReturn PermissionSet()
            on { this.params(":name") } doReturn reportName
        }

        val sut = ReportController(mockContext, orderly, mock<ZipClient>(),
                mock<OrderlyServerAPI>(),
                mockConfig)

        val result = sut.getLatestChangelogByName()
        assertThat(result.count()).isEqualTo(changelogs.count())
        for (i in 0 until result.count() - 1)
        {
            assertThat(result[i]).isEqualTo(changelogs[i])
        }
    }

}
