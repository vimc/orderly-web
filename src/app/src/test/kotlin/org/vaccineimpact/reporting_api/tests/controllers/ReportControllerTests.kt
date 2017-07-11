package org.vaccineimpact.reporting_api.tests.controllers

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.vaccineimpact.reporting_api.ActionContext
import org.vaccineimpact.reporting_api.OrderlyClient
import org.vaccineimpact.reporting_api.controllers.ReportController
import org.vaccineimpact.reporting_api.models.OrderlyReport

class ReportControllerTests{

    @Test
    fun `getReports returns all report names`() {
        val reportNames = listOf("testname1", "testname2")

        val orderly = mock<OrderlyClient> {
            on { this.getAllReports() } doReturn reportNames
        }
        val sut = ReportController(orderly)

        assertThat(sut.getAll(mock<ActionContext>())).isEqualTo(reportNames)
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

        assertThat(sut.getByName(actionContext)).isEqualTo(reportVersions)
    }

    @Test
    fun `getByNameAndVersion returns report metadata`() {

        val reportName = "reportName"
        val reportVersion = "reportVersion"

        val report = OrderlyReport(reportVersion,
                reportName, "views", "data",
                "artefacts", "date")

        val orderly = mock<OrderlyClient> {
            on { this.getReportsByNameAndVersion(reportName, reportVersion) } doReturn report
        }

        val actionContext = mock<ActionContext> {
            on {this.params(":version")} doReturn reportVersion
            on {this.params(":name")} doReturn reportName
        }

        val sut = ReportController(orderly)

        assertThat(sut.getByNameAndVersion(actionContext)).isEqualTo(report)
    }

}
