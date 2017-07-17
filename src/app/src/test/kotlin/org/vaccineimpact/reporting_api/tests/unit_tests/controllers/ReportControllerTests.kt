package org.vaccineimpact.reporting_api.tests.unit_tests.controllers

import com.google.gson.JsonParser
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import org.vaccineimpact.reporting_api.*
import org.vaccineimpact.reporting_api.controllers.ReportController
import org.vaccineimpact.reporting_api.db.Config
import org.vaccineimpact.reporting_api.db.OrderlyClient
import org.vaccineimpact.reporting_api.test_helpers.MontaguTests
import spark.Response
import javax.servlet.ServletOutputStream
import javax.servlet.http.HttpServletResponse

class ReportControllerTests: ControllerTest()
{

    @Test
    fun `getReports returns all report names`() {
        val reportNames = listOf("testname1", "testname2")

        val orderly = mock<OrderlyClient> {
            on { this.getAllReports() } doReturn reportNames
        }
        val sut = ReportController(orderly)

        assertThat(sut.getAllNames(mock<ActionContext>())).isEqualTo(reportNames)
    }

    @Test
    fun `getByName returns all reports versions by name`() {

        val reportName = "reportName"
        val reportVersions = listOf("version1", "version2")

        val orderly = mock<OrderlyClient> {
            on { this.getReportsByName(reportName) } doReturn reportVersions
        }

        val actionContext = mock<ActionContext> {
            on {this.params(":name")} doReturn reportName
        }

        val sut = ReportController(orderly)

        assertThat(sut.getVersionsByName(actionContext)).isEqualTo(reportVersions)
    }

    @Test
    fun `getByNameAndVersion returns report metadata`() {

        val reportName = "reportName"
        val reportVersion = "reportVersion"

        val report = JsonParser().parse("{\"key\":\"value\"}")

        val orderly = mock<OrderlyClient> {
            on { this.getReportsByNameAndVersion(reportName, reportVersion) } doReturn report.asJsonObject
        }

        val actionContext = mock<ActionContext> {
            on {this.params(":version")} doReturn reportVersion
            on {this.params(":name")} doReturn reportName
        }

        val sut = ReportController(orderly)

        assertThat(sut.getByNameAndVersion(actionContext)).isEqualTo(report)
    }

    @Test
    fun `getZippedByNameAndVersion returns zip file`() {

        val reportName = "reportName"
        val reportVersion = "reportVersion"

        val actionContext = mock<ActionContext> {
            on {this.params(":version")} doReturn reportVersion
            on {this.params(":name")} doReturn reportName
            on {this.getSparkResponse()} doReturn mockSparkResponse
        }

        val mockZipClient = mock<ZipClient>()

        val sut = ReportController(mock<OrderlyClient>(), mockZipClient)

        sut.getZippedByNameAndVersion(actionContext)

        verify(mockZipClient, times(1)).zipIt("${Config["orderly.root"]}archive/$reportName/$reportVersion/"
                , mockOutputStream)
    }

}
