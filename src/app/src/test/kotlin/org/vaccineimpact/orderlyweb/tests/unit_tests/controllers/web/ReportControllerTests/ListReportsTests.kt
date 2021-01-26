package org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.web.ReportControllerTests

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.Ignore
import org.junit.Test
import org.vaccineimpact.orderlyweb.*
import org.vaccineimpact.orderlyweb.controllers.web.ReportController
import org.vaccineimpact.orderlyweb.db.repositories.ReportRepository
import org.vaccineimpact.orderlyweb.models.ReportVersionWithDescLatest
import java.time.Instant

class ListReportsTests
{
    @Test
    fun `can list all reports`()
    {
        val mockContext: ActionContext = mock()
        val mockOrderlyServer: OrderlyServerAPI = mock {
            on { get("/reports/source", mockContext) } doReturn
                OrderlyServerResponse(
                    Serializer.instance.toResult(
                        listOf(
                            "report1",
                            "report2"
                        )
                    ), 200
                )
        }
        val date1 = Instant.now()
        val date2 = date1.minusSeconds(60)
        val mockReportRepo: ReportRepository = mock {
            on { this.getAllReportVersions() } doReturn
                listOf(
                    ReportVersionWithDescLatest("report1", null, "ID", false, date1, "VERSION", null),
                    ReportVersionWithDescLatest("report2", null, "ID", false, date2, "VERSION", null)
                )
        }
        val sut = ReportController(mockContext, mock(), mockOrderlyServer, mockReportRepo, mock())
        val result = sut.getListReports()
        assertThat(result).isEqualTo(
            listOf(
                mapOf("name" to "report1", "date" to date1),
                mapOf("name" to "report2", "date" to date2)
            )
        )
    }

    @Test
    fun `can list reports for branch and commit`()
    {
        val mockContext: ActionContext = mock {
            on { queryParams("branch") } doReturn "branch1"
            on { queryParams("commit") } doReturn "abcdef1"
        }
        val mockOrderlyServer: OrderlyServerAPI = mock {
            on { get("/reports/source", mockContext) } doReturn
                OrderlyServerResponse(
                    Serializer.instance.toResult(
                        listOf(
                            "report_that_has_been_run",
                            "report_that_has_not_been_run"
                        )
                    ), 200
                )
        }
        val now = Instant.now()
        val mockReportRepo: ReportRepository = mock {
            on { this.getAllReportVersions() } doReturn
                listOf(
                    ReportVersionWithDescLatest(
                        "report_that_has_been_run",
                        null,
                        "ID",
                        false,
                        now,
                        "VERSION",
                        null
                    )
                )
        }
        val sut = ReportController(mockContext, mock(), mockOrderlyServer, mockReportRepo, mock())
        val result = sut.getListReports()
        assertThat(result).isEqualTo(
            listOf(
                mapOf("name" to "report_that_has_been_run", "date" to now),
                mapOf("name" to "report_that_has_not_been_run", "date" to null)
            )
        )
    }
}
