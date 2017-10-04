package org.vaccineimpact.reporting_api.tests.unit_tests.controllers

import com.google.gson.JsonParser
import com.nhaarman.mockito_kotlin.*
import khttp.responses.Response
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.reporting_api.ActionContext
import org.vaccineimpact.reporting_api.OrderlyServerAPI
import org.vaccineimpact.reporting_api.ZipClient
import org.vaccineimpact.reporting_api.controllers.ReportController
import org.vaccineimpact.reporting_api.db.Config
import org.vaccineimpact.reporting_api.db.OrderlyClient

class ReportControllerTests : ControllerTest()
{

    @Test
    fun `runs a report`()
    {
        val reportName = "report1"
        val actionContext = mock<ActionContext> {
            on { this.params(":name") } doReturn reportName
        }

        val mockAPIResponse = mock<Response>(){
            on { this.text } doReturn "okayresponse"
        }

        val apiClient = mock<OrderlyServerAPI>(){
            on { this.post(any(), any())} doReturn mockAPIResponse
        }

        val sut = ReportController(actionContext, mock<OrderlyClient>(),
                mock<ZipClient>(), apiClient)

        val result = sut.run()

        assertThat(result).isEqualTo("okayresponse")
    }

    @Test
    fun `getReports returns all report names`()
    {
        val reportNames = listOf("testname1", "testname2")

        val orderly = mock<OrderlyClient> {
            on { this.getAllReports() } doReturn reportNames
        }
        val sut = ReportController(mock<ActionContext>(), orderly,
                mock<ZipClient>(), mock<OrderlyServerAPI>())

        assertThat(sut.getAllNames()).isEqualTo(reportNames)
    }

    @Test
    fun `getByName returns all reports versions by name`()
    {

        val reportName = "reportName"
        val reportVersions = listOf("version1", "version2")

        val orderly = mock<OrderlyClient> {
            on { this.getReportsByName(reportName) } doReturn reportVersions
        }

        val actionContext = mock<ActionContext> {
            on { this.params(":name") } doReturn reportName
        }

        val sut = ReportController(actionContext, orderly, mock<ZipClient>(), mock<OrderlyServerAPI>())

        assertThat(sut.getVersionsByName()).isEqualTo(reportVersions)
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
            on { this.params(":version") } doReturn reportVersion
            on { this.params(":name") } doReturn reportName
        }

        val sut = ReportController(actionContext, orderly, mock<ZipClient>(), mock<OrderlyServerAPI>())

        assertThat(sut.getByNameAndVersion()).isEqualTo(report)
    }

    @Test
    fun `getZippedByNameAndVersion returns zip file`()
    {

        val reportName = "reportName"
        val reportVersion = "reportVersion"

        val actionContext = mock<ActionContext> {
            on { this.params(":version") } doReturn reportVersion
            on { this.params(":name") } doReturn reportName
            on { this.getSparkResponse() } doReturn mockSparkResponse
        }

        val mockZipClient = mock<ZipClient>()

        val sut = ReportController(actionContext, mock<OrderlyClient>(), mockZipClient, mock<OrderlyServerAPI>())

        sut.getZippedByNameAndVersion()

        verify(mockZipClient, times(1)).zipIt("${Config["orderly.root"]}archive/$reportName/$reportVersion/"
                , mockOutputStream)
    }

}
