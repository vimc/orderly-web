package org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.api

import com.google.gson.Gson
import com.nhaarman.mockito_kotlin.*
import okhttp3.Headers
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.pac4j.core.profile.CommonProfile
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.OrderlyServerAPI
import org.vaccineimpact.orderlyweb.PorcelainResponse
import org.vaccineimpact.orderlyweb.controllers.api.ReportRunController
import org.vaccineimpact.orderlyweb.db.repositories.ReportRunRepository
import org.vaccineimpact.orderlyweb.errors.PorcelainError
import java.time.Instant

class ReportRunControllerTests : ControllerTest()
{
    private val reportName = "report1"
    private val reportKey = "123"

    @Test
    fun `runs a report`()
    {
        val params = mapOf("param" to "p1")
        val changelog = mapOf(
                "message" to "test message",
                "type" to "test type"
        )

        val actionContext: ActionContext = mock {
            on { params(":name") } doReturn reportName
            on { postData<Any>() } doReturn mapOf(
                    "instances" to mapOf("instance" to "i1"),
                    "params" to params,
                    "changelog" to changelog,
                    "gitBranch" to "branch1",
                    "gitCommit" to "abc123"
            )
            on { queryParams("timeout") } doReturn "600"
            on { userProfile } doReturn CommonProfile().apply { id = "a@b.com" }
        }

        val mockAPIResponseText =
                """{"data": {"name": "$reportName", "key": $reportKey, "path": "/status/$reportKey"}}"""

        val mockAPIResponse = PorcelainResponse(mockAPIResponseText, 200, Headers.headersOf())

        val expectedQs = mapOf(
                "ref" to "abc123",
                "instance" to "i1",
                "timeout" to "600"
        )
        val apiClient: OrderlyServerAPI = mock {
            on {
                post(
                        eq("/v1/reports/$reportName/run/"),
                        eq(
                                Gson().toJson(
                                        mapOf(
                                                "params" to params,
                                                "changelog" to changelog
                                        )
                                )
                        ),
                        eq(expectedQs),
                        any()
                )
            } doReturn mockAPIResponse
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
                eq(params),
                eq("branch1"),
                eq("abc123")
        )
    }

    @Test
    fun `runs a report without arguments`()
    {
        val actionContext: ActionContext = mock {
            on { params(":name") } doReturn reportName
            on { userProfile } doReturn CommonProfile().apply { id = "a@b.com" }
        }

        val mockAPIResponseText =
                """{"data": {"name": "$reportName", "key": $reportKey, "path": "/status/$reportKey"}}"""

        val mockAPIResponse = PorcelainResponse(mockAPIResponseText, 200, Headers.headersOf())

        val expectedBody = """{"params":{}}"""
        val apiClient: OrderlyServerAPI = mock {
            on { post("/v1/reports/$reportName/run/", expectedBody, mapOf()) } doReturn mockAPIResponse
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
                eq(mapOf()),
                eq(mapOf()),
                eq(null),
                eq(null)
        )
    }

    @Test
    fun `runs a report does not insert record on failure`()
    {
        val actionContext: ActionContext = mock {
            on { params(":name") } doReturn reportName
        }

        val url = "/v1/reports/$reportName/run/"
        val expectedBody = "{\"params\":{}}"
        val apiClient: OrderlyServerAPI = mock {
            on { post(url, expectedBody, mapOf()) } doThrow PorcelainError(url, 500, "Orderly server", listOf())
        }

        val mockReportRunRepo: ReportRunRepository = mock()

        val sut = ReportRunController(actionContext, mockReportRunRepo, apiClient, mock())

        assertThatThrownBy { sut.run() }
                .isInstanceOf(PorcelainError::class.java)
                .matches { (it as PorcelainError).httpStatus == 500 }
        verifyZeroInteractions(mockReportRunRepo)
    }

    @Test
    fun `gets report status`()
    {
        val actionContext: ActionContext = mock {
            on { params(":key") } doReturn reportKey
        }

        val mockAPIResponse = PorcelainResponse("""{"status": "running"}""", 200, Headers.headersOf())

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

        val mockAPIResponse = PorcelainResponse("okayresponse", 200, Headers.headersOf())

        val apiClient: OrderlyServerAPI = mock {
            on { delete("/v1/reports/$reportKey/kill/", actionContext) } doReturn mockAPIResponse
        }

        val sut = ReportRunController(actionContext, mock(), apiClient, mock())
        val result = sut.kill()

        assertThat(result).isEqualTo("okayresponse")
    }
}
