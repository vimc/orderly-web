package org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.api

import com.nhaarman.mockito_kotlin.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.pac4j.core.profile.CommonProfile
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.OrderlyServerAPI
import org.vaccineimpact.orderlyweb.OrderlyServerResponse
import org.vaccineimpact.orderlyweb.controllers.api.ReportRunController
import org.vaccineimpact.orderlyweb.db.repositories.ReportRunRepository
import java.time.Instant

class ReportRunControllerTests : ControllerTest()
{
    private val reportName = "report1"
    private val reportKey = "123"

    @Test
    fun `runs a report`()
    {
        val actionContext: ActionContext = mock {
            on { params(":name") } doReturn reportName
            on { postData<Any>() } doReturn mapOf(
                "instances" to mapOf("instance" to "i1"),
                "params" to mapOf("param" to "p1"),
                "gitBranch" to "branch1",
                "gitCommit" to "abc123"
            )
            on { userProfile } doReturn CommonProfile().apply { id = "a@b.com" }
        }

        val mockAPIResponseText =
            """{"data": {"name": "$reportName", "key": $reportKey, "path": "/status/$reportKey"}}"""

        val mockAPIResponse = OrderlyServerResponse(mockAPIResponseText, 200)

        val apiClient: OrderlyServerAPI = mock {
            on { post(any<String>(), any<String>(), any<Map<String, String?>>()) } doReturn mockAPIResponse
        }

        val mockReportRunRepo: ReportRunRepository = mock()

        val sut = ReportRunController(actionContext, mockReportRunRepo, apiClient, mock())
        val result = sut.run()

        assertThat(result).isEqualTo(mockAPIResponseText)
        verify(mockReportRunRepo).addReportRun(
            eq(reportKey),
            eq("a@b.com"),
            any<Instant>(),
            eq(reportName),
            eq(mapOf("instance" to "i1")),
            eq(mapOf("param" to "p1")),
            eq("branch1"),
            eq("abc123")
        )
    }

    @Test
    fun `gets report status`()
    {
        val actionContext: ActionContext = mock {
            on { params(":key") } doReturn reportKey
        }

        val mockAPIResponse = OrderlyServerResponse("""{"status": "running"}""", 200)

        val apiClient: OrderlyServerAPI = mock {
            on { get("/v1/reports/$reportKey/status/", actionContext) } doReturn mockAPIResponse
        }

        val sut = ReportRunController(actionContext, mock(), apiClient, mock())
        val result = sut.status()

        assertThat(result).isEqualTo("""{"status": "running"}""")
    }

    @Test
    fun `kills a report`()
    {
        val actionContext: ActionContext = mock {
            on { params(":key") } doReturn reportKey
        }

        val mockAPIResponse = OrderlyServerResponse("okayresponse", 200)

        val apiClient: OrderlyServerAPI = mock {
            on { delete("/v1/reports/$reportKey/kill/", actionContext) } doReturn mockAPIResponse
        }

        val sut = ReportRunController(actionContext, mock(), apiClient, mock())
        val result = sut.kill()

        assertThat(result).isEqualTo("okayresponse")
    }
}
