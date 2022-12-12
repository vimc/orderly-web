package org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.web.reportControllerTests

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.Test
import org.vaccineimpact.orderlyweb.*
import org.vaccineimpact.orderlyweb.controllers.web.ReportController
import org.vaccineimpact.orderlyweb.db.repositories.ReportRepository
import org.vaccineimpact.orderlyweb.models.ReportWithDate
import java.time.Instant

class RunnableReportsTests
{
    @Test
    fun `can list all reports`()
    {
        val mockQueryParams: Map<String, String> = mapOf()
        val mockContext: ActionContext = mock<ActionContext>{
            on { queryParams() } doReturn mockQueryParams
        }
        val reports = listOf(
            "report1",
            "report2"
        )
        val mockOrderlyServer: OrderlyServerAPI = mock {
            on { getRunnableReportNames(mockQueryParams) } doReturn reports
        }
        val date1 = Instant.now()
        val date2 = date1.minusSeconds(60)
        val mockReportRepo: ReportRepository = mock {
            on { getLatestReportVersions(reports) } doReturn
                listOf(
                    ReportWithDate("report1", date1),
                    ReportWithDate("report2", date2)
                )
        }
        val sut = ReportController(mockContext, mock(), mockOrderlyServer, mockReportRepo, mock())
        val result = sut.getRunnableReports()
        assertThat(result).isEqualTo(
            listOf(
                ReportWithDate("report1", date1),
                ReportWithDate("report2", date2)
            )
        )
    }

    @Test
    fun `can list reports for branch and commit`()
    {
        val mockQueryParams = mapOf("branch" to "branch1", "commit" to "abcdef1")
        val mockContext: ActionContext = mock {
            on { queryParams() } doReturn mockQueryParams
        }
        val reports = listOf(
            "report_that_has_been_run",
            "report_that_has_not_been_run"
        )
        val mockOrderlyServer: OrderlyServerAPI = mock {
            on { getRunnableReportNames(mockQueryParams) } doReturn reports
        }
        val now = Instant.now()
        val mockReportRepo: ReportRepository = mock {
            on { getLatestReportVersions(reports) } doReturn
                listOf(
                    ReportWithDate("report_that_has_been_run", now)
                )
        }
        val sut = ReportController(mockContext, mock(), mockOrderlyServer, mockReportRepo, mock())
        val result = sut.getRunnableReports()
        assertThat(result).isEqualTo(
            listOf(
                ReportWithDate("report_that_has_been_run", now),
                ReportWithDate("report_that_has_not_been_run", null)
            )
        )
    }
}
