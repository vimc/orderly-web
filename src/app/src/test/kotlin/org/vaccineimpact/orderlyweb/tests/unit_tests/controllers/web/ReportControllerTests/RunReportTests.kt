package org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.web.ReportControllerTests

import com.nhaarman.mockito_kotlin.mock
import org.junit.Test
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.web.ReportController
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests

class RunReportTests: TeamcityTests()
{
    @Test
    fun `getRunReport creates viewmodel`()
    {
        val mockContext = mock<ActionContext>()
        val sut = ReportController(mockContext, mock(), mock(), mock())
        val result = sut.getRunReport()
        assertThat(result.breadcrumbs.count()).isEqualTo(2)
        assertThat(result.breadcrumbs[1].name).isEqualTo("Run a report")
        assertThat(result.breadcrumbs[1].url).isEqualTo("http://localhost:8888/run-report")
    }
}