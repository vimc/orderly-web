package org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.web.reportControllerTests

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThatThrownBy
import org.junit.Test
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.web.ReportController
import org.vaccineimpact.orderlyweb.db.repositories.ReportRepository
import org.vaccineimpact.orderlyweb.errors.BadRequest

class PinnedReportTests
{
    private val reports = listOf("r1", "r2")

    private val mockContext = mock<ActionContext> {
        on { postData<List<String>>("reports") } doReturn reports
    }

    private val mockRepo = mock<ReportRepository> {
        on { reportExists("r1") } doReturn true
        on { reportExists("r2") } doReturn true
    }

    @Test
    fun `setGlobalPinnedReports calls repo`()
    {
        val sut = ReportController(mockContext, mock(), mock(), mockRepo, mock())
        val result = sut.setGlobalPinnedReports()
        assertThat(result).isEqualTo("OK")
        verify(mockRepo).setGlobalPinnedReports(reports)
    }

    @Test
    fun `setGlobalPinnedReports throws error if report does not exist`()
    {
        val notFoundRepo = mock<ReportRepository> {
            on { reportExists("r1") } doReturn true
            on { reportExists("r2") } doReturn false
        }

        val sut = ReportController(mockContext, mock(), mock(), notFoundRepo, mock())
        assertThatThrownBy { sut.setGlobalPinnedReports() }.isInstanceOf(BadRequest::class.java)
                .hasMessageContaining("Report 'r2' does not exist")
    }

    @Test
    fun `setGlobalPinnedReport throws error if duplicate reports`()
    {
        val dupesContext = mock<ActionContext> {
            on { postData<List<String>>("reports") } doReturn listOf("r1", "r1")
        }

        val sut = ReportController(dupesContext, mock(), mock(), mockRepo, mock())
        assertThatThrownBy { sut.setGlobalPinnedReports() }.isInstanceOf(BadRequest::class.java)
                .hasMessageContaining("Cannot include the same pinned report twice")
    }
}
