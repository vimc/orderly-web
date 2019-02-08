package org.vaccineimpact.reporting_api.tests.unit_tests.controllers

import com.google.gson.JsonObject
import com.nhaarman.mockito_kotlin.*
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import java.time.Instant
import org.junit.Test
import org.vaccineimpact.api.models.*

import org.vaccineimpact.api.models.permissions.PermissionSet
import org.vaccineimpact.api.models.permissions.ReifiedPermission
import org.vaccineimpact.reporting_api.ActionContext
import org.vaccineimpact.reporting_api.OrderlyServerAPI
import org.vaccineimpact.reporting_api.ZipClient
import org.vaccineimpact.reporting_api.controllers.VersionController
import org.vaccineimpact.reporting_api.db.Config
import org.vaccineimpact.reporting_api.db.OrderlyClient
import org.vaccineimpact.reporting_api.errors.UnknownObjectError


class VersionControllerTests : ControllerTest()
{
    private val mockConfig = mock<Config> {
        on { this.get("orderly.root") } doReturn "root/"
    }

    @Test
    fun `getByNameAndVersion returns report metadata`()
    {
        val report = JsonObject()
        val orderly = mock<OrderlyClient> {
            on { this.getReportByNameAndVersion(reportName, reportVersion) } doReturn report
        }

        val actionContext = mock<ActionContext> {
            on { this.permissions } doReturn PermissionSet()
            on { this.params(":version") } doReturn reportVersion
            on { this.params(":name") } doReturn reportName
        }

        val sut = VersionController(actionContext, orderly, mock<ZipClient>(),
                mock<OrderlyServerAPI>(),
                mockConfig)

        assertThat(sut.getByNameAndVersion()).isEqualTo(report)
    }

    @Test
    fun `getDetailsByNameAndVersion returns report metadata`()
    {
        val reportName = "reportName"
        val reportVersion = "reportVersion"

        val report = ReportVersionDetails(author = "author", displayName = "displayName", id = "id", date = Instant.now(),
                name = "name", published = true, requester = "requester", description = "description",
                comment = "comment", script = "script", hashScript = "hashscript")

        val orderly = mock<OrderlyClient> {
            on { this.getDetailsByNameAndVersion(reportName, reportVersion) } doReturn report
        }

        val actionContext = mock<ActionContext> {
            on { this.permissions } doReturn PermissionSet()
            on { this.params(":version") } doReturn reportVersion
            on { this.params(":name") } doReturn reportName
        }

        val sut = VersionController(actionContext, orderly, mock<ZipClient>(),
                mock<OrderlyServerAPI>(),
                mockConfig)

        assertThat(sut.getDetailsByNameAndVersion()).isEqualTo(report)
    }

    @Test
    fun `getChangelogByNameAndVersion returns changelog`()
    {
        val changelogs = listOf(Changelog(reportVersion, "public", "did a thing", true),
                Changelog(reportVersion, "public", "did another thing", true))

        val orderly = mock<OrderlyClient> {
            on { this.getChangelogByNameAndVersion(reportName, reportVersion) } doReturn changelogs
        }

        val mockContext = mock<ActionContext> {
            on { this.permissions } doReturn PermissionSet()
            on { this.params(":version") } doReturn reportVersion
            on { this.params(":name") } doReturn reportName
        }

        val sut = VersionController(mockContext, orderly, mock<ZipClient>(),
                mock<OrderlyServerAPI>(),
                mockConfig)

        val result = sut.getChangelogByNameAndVersion()
        assertThat(result.count()).isEqualTo(changelogs.count())
        for (i in 0 until result.count() - 1)
        {
            assertThat(result[i]).isEqualTo(changelogs[i])
        }
    }

    @Test
    fun `getZippedByNameAndVersion returns zip of all files if user is a reviewer`()
    {
        val actionContext = mock<ActionContext> {
            on { this.params(":version") } doReturn reportVersion
            on { this.params(":name") } doReturn reportName
            on { this.getSparkResponse() } doReturn mockSparkResponse
            on { this.permissions } doReturn PermissionSet(setOf(ReifiedPermission("reports.read", Scope.Global()),
                    ReifiedPermission("reports.review", Scope.Global())))
        }

        val mockZipClient = mock<ZipClient>()
        val sut = VersionController(actionContext, mock(), mockZipClient, mock(),
                mockConfig)

        sut.getZippedByNameAndVersion()

        verify(mockZipClient, times(1)).zipIt("root/archive/$reportName/$reportVersion/"
                , mockOutputStream, ".*")
    }

    @Test
    fun `getZippedByNameAndVersion only returns artefacts and resources if user is not a reviewer`()
    {
        val actionContext = makeMockReportReadingContext()

        val mockZipClient = mock<ZipClient>()
        val mockOrderlyClient = mock<OrderlyClient> {
            on { getArtefacts(reportName, reportVersion) } doReturn listOf(Artefact(ArtefactFormat.DATA,
                    "some desc", listOf("file1.csv", "file2.pdf")))
            on { getResourceFileNames(reportName, reportVersion) } doReturn listOf("/meta/inputs1.rds", "table.xlsx")
        }

        val sut = VersionController(actionContext, mockOrderlyClient, mockZipClient, mock(),
                mockConfig)

        sut.getZippedByNameAndVersion()

        val sourcePath = "root/archive/$reportName/$reportVersion/"
        verify(mockZipClient, times(1)).zipIt(sourcePath,
                mockOutputStream, "$sourcePath(file1.csv|file2.pdf|/meta/inputs1.rds|table.xlsx)")
    }

    @Test
    fun `getZippedByNameAndVersion checks that report exists`()
    {
        val actionContext = makeMockReportReadingContext()

        val mockZipClient = mock<ZipClient>()
        val mockOrderlyClient = mock<OrderlyClient> {
            on { getReportByNameAndVersion(reportName, reportVersion) } doThrow
                    UnknownObjectError(reportVersion, "report")
        }
        val sut = VersionController(actionContext, mockOrderlyClient, mockZipClient, mock(),
                mockConfig)

        Assertions.assertThatThrownBy { sut.getZippedByNameAndVersion() }
                .isInstanceOf(UnknownObjectError::class.java)

    }

    private val reportName: String = "Report name"
    private val reportVersion: String = "Report version"

    private fun makeMockReportReadingContext(): ActionContext
    {
        return mock {
            on { this.params(":version") } doReturn reportVersion
            on { this.params(":name") } doReturn reportName
            on { this.getSparkResponse() } doReturn mockSparkResponse
            on { this.permissions } doReturn PermissionSet(setOf(ReifiedPermission("reports.read", Scope.Global())))
        }
    }

}