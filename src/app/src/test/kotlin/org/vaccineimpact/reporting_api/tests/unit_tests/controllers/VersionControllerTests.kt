package org.vaccineimpact.reporting_api.tests.unit_tests.controllers

import com.google.gson.JsonParser
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.vaccineimpact.api.models.Changelog
import org.vaccineimpact.api.models.permissions.PermissionSet
import org.vaccineimpact.reporting_api.ActionContext
import org.vaccineimpact.reporting_api.OrderlyServerAPI
import org.vaccineimpact.reporting_api.ZipClient
import org.vaccineimpact.reporting_api.controllers.VersionController
import org.vaccineimpact.reporting_api.db.Config
import org.vaccineimpact.reporting_api.db.OrderlyClient
import org.vaccineimpact.reporting_api.errors.UnknownObjectError
import org.vaccineimpact.reporting_api.tests.createArchiveFolder
import org.vaccineimpact.reporting_api.tests.deleteArchiveFolder


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

        val report = JsonParser().parse("{\"key\":\"value\"}")

        val orderly = mock<OrderlyClient> {
            on { this.getReportsByNameAndVersion(reportName, reportVersion) } doReturn report.asJsonObject
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
    fun `getChangelogByNameAndVersion returns changelog`()
    {
        val reportName = "reportName"
        val reportVersion = "reportVersion"

        val changelogs = listOf(Changelog(reportVersion, "public", "did a thing", true),
                Changelog(reportVersion,"public", "did another thing", true))

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
        for(i in 0 until result.count()-1)
        {
            assertThat(result[i]).isEqualTo(changelogs[i])
        }
    }

    @Test
    fun `getZippedByNameAndVersion returns zip file`()
    {
        val reportName = "reportName"
        val reportVersion = "reportVersion"

        createArchiveFolder(reportName, reportVersion, mockConfig)

        try
        {
            val actionContext = mock<ActionContext> {
                on { this.params(":version") } doReturn reportVersion
                on { this.params(":name") } doReturn reportName
                on { this.getSparkResponse() } doReturn mockSparkResponse
                on { this.permissions } doReturn PermissionSet()
            }

            val mockZipClient = mock<ZipClient>()

            val sut = VersionController(actionContext, mock<OrderlyClient>(), mockZipClient, mock<OrderlyServerAPI>(),
                    mockConfig)

            sut.getZippedByNameAndVersion()

            verify(mockZipClient, times(1)).zipIt("root/archive/$reportName/$reportVersion/"
                    , mockOutputStream)
        }
        finally
        {
            deleteArchiveFolder(reportName, reportVersion, mockConfig)
        }

    }

    @Test
    fun `getZippedByNameAndVersion throws UnknwonObjectError for nonexistent version`()
    {
        val reportName = "reportName"
        val reportVersion = "reportVersion"

        val actionContext = mock<ActionContext> {
            on { this.params(":version") } doReturn reportVersion
            on { this.params(":name") } doReturn reportName
            on { this.getSparkResponse() } doReturn mockSparkResponse
            on { this.permissions } doReturn PermissionSet()
        }

        val mockZipClient = mock<ZipClient>()
        val sut = VersionController(actionContext, mock<OrderlyClient>(), mockZipClient, mock<OrderlyServerAPI>(),
                mockConfig)

        Assertions.assertThatThrownBy { sut.getZippedByNameAndVersion() }
                .isInstanceOf(UnknownObjectError::class.java)
    }


}