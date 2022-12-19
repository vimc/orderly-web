package org.vaccineimpact.orderlyweb.tests.unit_tests

import com.nhaarman.mockito_kotlin.*
import okhttp3.*
import okhttp3.ResponseBody.Companion.toResponseBody
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.vaccineimpact.orderlyweb.*
import org.vaccineimpact.orderlyweb.db.Config
import org.vaccineimpact.orderlyweb.errors.PorcelainError

class OutpackServerClientTests
{

    private val mockConfig = mock<Config> {
        on { this["outpack.server"] } doReturn "http://outpack"
    }

    @Test
    fun `configures correct url and instance name`()
    {
        val httpclient = getHttpClient(responseCode = 500)
        val sut = OutpackServerClient(mockConfig, httpclient)

        assertThat(sut is PorcelainAPI)
        assertThatThrownBy {
            sut.throwOnError()
                    .get("/some/path/", context = mock())
        }.isInstanceOf(PorcelainError::class.java)
                .hasMessageContaining("Outpack server request failed")

        verify(httpclient).newCall(
                check {
                    assertThat(it.url.toString()).isEqualTo("http://outpack/some/path/")
                }
        )
    }

    private fun getHttpClient(
            responseBody: String = """{"data": [], "errors": null, "status": "success"}""",
            responseCode: Int = 200
    ): OkHttpClient
    {
        val response = Response.Builder()
                .request(Request.Builder().url("http://outpack").build())
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

