package org.vaccineimpact.orderlyweb.tests.unit_tests

import com.nhaarman.mockito_kotlin.*
import okhttp3.*
import okhttp3.Headers.Companion.toHeaders
import okhttp3.ResponseBody.Companion.toResponseBody
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.vaccineimpact.orderlyweb.ContentTypes
import org.vaccineimpact.orderlyweb.OrderlyServerClient
import org.vaccineimpact.orderlyweb.*
import org.vaccineimpact.orderlyweb.db.Config
import org.vaccineimpact.orderlyweb.errors.PorcelainError
import org.vaccineimpact.orderlyweb.models.Parameter

class OrderlyServerClientTests
{

    private val mockConfig = mock<Config> {
        on { this["orderly.server"] } doReturn "http://orderly"
    }

    private val standardHeaders = mapOf("Accept" to ContentTypes.json)

    @Test
    fun `configures correct url and instance name`()
    {
        val httpClient = getHttpClient(responseCode = 500)
        val sut = OrderlyServerClient(mockConfig, httpClient)

        assertThat(sut is PorcelainAPI)
        assertThatThrownBy {
            sut.throwOnError()
                    .get("/some/path/", context = mock())
        }.isInstanceOf(PorcelainError::class.java)
                .hasMessageContaining("Orderly server request failed")

        verify(httpClient).newCall(
                check {
                    assertThat(it.url.encodedPath).isEqualTo("/some/path/")
                }
        )
    }

    @Test
    fun `getRunnableReportNames does expected get`()
    {
        val mockResponse = """{"data": ["report1", "report2"], "errors": null, "status": "success"}"""
        val mockQueryParams = mapOf("branch" to "testBranch", "commit" to "testCommit")
        val client = getHttpClient(mockResponse)
        val result = OrderlyServerClient(mockConfig, client).getRunnableReportNames(mockQueryParams)
        assertThat(result).hasSameElementsAs(listOf("report1", "report2"))

        verify(client).newCall(
                check {
                    assertThat(it.url.toString()).isEqualTo("http://orderly/reports/source?branch=testBranch&commit=testCommit")
                    assertThat(it.headers).isEqualTo(standardHeaders.toHeaders())
                }
        )
    }

    @Test
    fun `getReportParameters does expected get`()
    {
        val mockResponse = """{"data": [
                {"name": "param1", "value": "1"},
                {"name": "param2", "value": "2"}
            ],
            "errors": null, "status": "success"}""".trimMargin()

        val mockQueryParams = mapOf("commit" to "testCommit")
        val httpClient = getHttpClient(mockResponse)
        val result = OrderlyServerClient(mockConfig, httpClient).getReportParameters("report1", mockQueryParams)
        assertThat(result).hasSameElementsAs(
                listOf(
                        Parameter("param1", "1"),
                        Parameter("param2", "2")
                )
        )

        verify(httpClient).newCall(
                check {
                    assertThat(it.url.toString()).isEqualTo("http://orderly/reports/report1/parameters?commit=testCommit")
                    assertThat(it.headers).isEqualTo(standardHeaders.toHeaders())
                }
        )
    }

    private fun getHttpClient(
            responseBody: String = """{"data": [], "errors": null, "status": "success"}""",
            responseCode: Int = 200
    ): OkHttpClient
    {
        val response = Response.Builder()
                .request(Request.Builder().url("http://orderly").build())
                .protocol(Protocol.HTTP_1_1)
                .code(responseCode)
                .header("Content-Type", ContentTypes.json)
                .message("OK")
                .body(responseBody.toResponseBody())
                .build()
        val call = mock<Call> {
            on { execute() } doReturn response
        }
        return mock {
            on { newCall(any()) } doReturn call
        }
    }

}

