package org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.web

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.web.ReportController
import org.vaccineimpact.orderlyweb.db.OrderlyClient
import org.vaccineimpact.orderlyweb.models.ReportVersionDetails
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests
import java.time.Instant

class ReportControllerTests : TeamcityTests()
{
    @Test
    fun `report page returns report details`()
    {
        val reportDetails = ReportVersionDetails("r1",
                "first report",
                "v1",
                true,
                Instant.now(),
                "dr author",
                "ms funder",
                "a fake report",
                listOf(),
                listOf(),
                mapOf())

        val orderly = mock<OrderlyClient> {
            on { this.getDetailsByNameAndVersion("r1", "v1") } doReturn reportDetails
        }

        val actionContext = mock<ActionContext> {
            on { this.params(":name") } doReturn "r1"
            on { this.params(":version") } doReturn "v1"
        }

        val sut = ReportController(actionContext, orderly)
        val result = sut.get()

        assertThat(result.report).isEqualTo(reportDetails)
    }
}