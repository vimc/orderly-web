package org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.api

import com.nhaarman.mockito_kotlin.*
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.orderlyweb.models.Changelog
import org.vaccineimpact.orderlyweb.models.ReportVersionDetails
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.PermissionSet
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.FileSystem
import org.vaccineimpact.orderlyweb.OrderlyServerAPI
import org.vaccineimpact.orderlyweb.ZipClient
import org.vaccineimpact.orderlyweb.controllers.api.VersionController
import org.vaccineimpact.orderlyweb.db.Config
import org.vaccineimpact.orderlyweb.db.OrderlyClient
import org.vaccineimpact.orderlyweb.errors.UnknownObjectError
import java.io.File
import java.time.Instant

class VersionControllerTests : ControllerTest()
{
    private val mockConfig = mock<Config> {
        on { this.get("orderly.root") } doReturn "root/"
    }

    @Test
    fun `getByNameAndVersion returns report metadata`()
    {
        val reportName = "reportName"
        val reportVersion = "reportVersion"

        val report = ReportVersionDetails(displayName = "displayName", id = "id", date = Instant.now(),
                name = "name", published = true, description = "description",
                artefacts = listOf(),
                resources = listOf(), dataInfo = listOf(), parameterValues = mapOf())

        val orderly = mock<OrderlyClient> {
            on { this.getDetailsByNameAndVersion(reportName, reportVersion) } doReturn report
        }

        val actionContext = mock<ActionContext> {
            on { this.permissions } doReturn PermissionSet()
            on { this.params(":version") } doReturn reportVersion
            on { this.params(":name") } doReturn reportName
        }

        val sut = VersionController(actionContext, orderly, mock<ZipClient>(),
                mock(),
                mock<OrderlyServerAPI>(),
                mockConfig)

        assertThat(sut.getByNameAndVersion()).isEqualTo(report)
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
                mock(),
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
            on { this.hasPermission(ReifiedPermission("reports.review", Scope.Global())) } doReturn true
        }

        val sourcePath = File("root/archive/$reportName/$reportVersion/").absolutePath
        val mockZipClient = mock<ZipClient>()
        val mockFiles = mock<FileSystem>() {
            on { getAllFilesInFolder(sourcePath)} doReturn arrayListOf("TEST")
        }
        val sut = VersionController(actionContext, mock(), mockZipClient, mockFiles, mock(), mockConfig)

        sut.getZippedByNameAndVersion()
        verify(mockZipClient, times(1)).zipIt(sourcePath, mockOutputStream, listOf("TEST"))
    }

    @Test
    fun `getZippedByNameAndVersion only returns artefacts and resources if user is not a reviewer`()
    {
        val actionContext = makeReportReaderActionContext()

        val mockZipClient = mock<ZipClient>()
        val mockOrderlyClient = mock<OrderlyClient> {
            on { getArtefactHashes(reportName, reportVersion) } doReturn mapOf("file1.csv" to "312", "file2.pdf" to "789")
            on { getResourceHashes(reportName, reportVersion) } doReturn mapOf("meta/inputs1.rds" to "123",
                    "table.xlsx" to "456")
        }

        val sut = VersionController(actionContext, mockOrderlyClient, mockZipClient, mock(),
                mock(), mockConfig)

        sut.getZippedByNameAndVersion()

        val sourcePath = File("root/archive/$reportName/$reportVersion/").absolutePath
        verify(mockZipClient, times(1)).zipIt(sourcePath, mockOutputStream,
                listOf("$sourcePath/file1.csv", "$sourcePath/file2.pdf", "$sourcePath/meta/inputs1.rds", "$sourcePath/table.xlsx"))
    }

    @Test
    fun `getZippedByNameAndVersion checks that report exists`()
    {
        val actionContext = makeReportReaderActionContext()

        val mockZipClient = mock<ZipClient>()
        val mockOrderlyClient = mock<OrderlyClient> {
            on { checkVersionExistsForReport(reportName, reportVersion) } doThrow
                    UnknownObjectError(reportVersion, "report")
        }
        val sut = VersionController(actionContext, mockOrderlyClient, mockZipClient, mock(),
                mock(),
                mockConfig)

        Assertions.assertThatThrownBy { sut.getZippedByNameAndVersion() }
                .isInstanceOf(UnknownObjectError::class.java)

    }

    private val reportName: String = "Report name"
    private val reportVersion: String = "Report version"

    private fun makeReportReaderActionContext(): ActionContext
    {
        return mock {
            on { this.params(":version") } doReturn reportVersion
            on { this.params(":name") } doReturn reportName
            on { this.getSparkResponse() } doReturn mockSparkResponse
        }
    }

}