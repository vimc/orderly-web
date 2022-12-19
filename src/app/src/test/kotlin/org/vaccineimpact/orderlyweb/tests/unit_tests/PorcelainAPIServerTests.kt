package org.vaccineimpact.orderlyweb.tests.unit_tests

import com.fasterxml.jackson.databind.ObjectMapper
import com.nhaarman.mockito_kotlin.*
import okhttp3.*
import okhttp3.Headers.Companion.toHeaders
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.vaccineimpact.orderlyweb.*
import org.vaccineimpact.orderlyweb.errors.PorcelainError
import org.vaccineimpact.orderlyweb.models.GitCommit

class PorcelainAPIServerTests
{

    private val standardHeaders = mapOf("Accept" to ContentTypes.json)

    @Test
    fun `passes through query string to GET`()
    {
        val mockContext = mock<ActionContext> {
            on { this.queryString() } doReturn "key1=val1"
        }
        val client = getHttpClient()
        PorcelainAPIServer("Fake instance", "http://url-base", client)
                .get("/some/path/", mockContext)

        verify(client).newCall(
                check {
                    assertThat(it.url.toString()).isEqualTo("http://url-base/some/path/?key1=val1")
                    assertThat(it.headers).isEqualTo(standardHeaders.toHeaders())
                }
        )
    }

    @Test
    fun `passes invalid query string using overloaded GET method`()
    {
        val client = getHttpClient()
        PorcelainAPIServer("Fake instance", "http://url-base", client)
                .get("/some/path/", emptyMap())

        verify(client).newCall(
                check {
                    assertThat(it.url.encodedPath).isEqualTo("/some/path/")
                    assertThat(it.headers).isEqualTo(standardHeaders.toHeaders())
                }
        )
    }

    @Test
    fun `passes valid query String using overloaded GET method`()
    {
        val client = getHttpClient()
        val key = "report"
        val nullKey = "emptyVal"
        val queryParams: Map<String, String> = mapOf(key to "minimal", nullKey to "")
        PorcelainAPIServer("Fake instance", "http://url-base", client)
                .get("/some/path/", queryParams)

        verify(client).newCall(
                check {
                    assertThat(it.url.toString()).isEqualTo("http://url-base/some/path/?report=minimal&emptyVal=")
                    assertThat(it.headers).isEqualTo(standardHeaders.toHeaders())
                }
        )
    }

    @Test
    fun `passes query parameters from URL`()
    {
        val client = getHttpClient()
        PorcelainAPIServer("Fake instance", "http://url-base", client)
                .get("/some/path?key1=val1", context = mock())

        verify(client).newCall(
                check {
                    assertThat(it.url.toString()).isEqualTo("http://url-base/some/path?key1=val1")
                }
        )
    }

    @Test
    fun `passes through JSON POST body`()
    {
        val mockContext = mock<ActionContext> {
            on { this.postData<String>() } doReturn mapOf("key1" to "val1")
        }
        val client = getHttpClient()
        PorcelainAPIServer("Fake instance", "http://url-base", client)
                .post("/some/path/", mockContext)

        verify(client).newCall(
                check {
                    assertThat(it.url.toString()).isEqualTo("http://url-base/some/path/")
                    assertThat(it.headers).isEqualTo(standardHeaders.toHeaders())
                    val buffer = Buffer()
                    it.body!!.writeTo(buffer)
                    assertThat(buffer.readUtf8()).isEqualTo("""{"key1":"val1"}""")
                }
        )
    }

    @Test
    fun `passes through binary POST body`()
    {
        val text = "foobar"
        val mockContext = mock<ActionContext> {
            on { getRequestBodyAsBytes() } doReturn text.toByteArray()
        }
        val client = getHttpClient()
        PorcelainAPIServer("Fake instance", "http://url-base", client)
                .post("/some/path/", mockContext, true)

        verify(client).newCall(
                check {
                    assertThat(it.url.toString()).isEqualTo("http://url-base/some/path/")
                    assertThat(it.headers).isEqualTo(standardHeaders.toHeaders())
                    val buffer = Buffer()
                    it.body!!.writeTo(buffer)
                    assertThat(buffer.readUtf8()).isEqualTo(text)
                }
        )
    }

    @Test
    fun `disabling response transformation works for POST`()
    {
        val text = """{"status":"failure","errors":[{"error":"FOO","detail":"bar"}],"data":null}"""
        val client = getHttpClient(text)
        val response = PorcelainAPIServer("Fake instance", "http://url-base", client)
                .post("/some/path/", mock(), transformResponse = false)

        verify(client).newCall(
                check {
                    assertThat(it.url.toString()).isEqualTo("http://url-base/some/path/")
                    assertThat(it.headers).isEqualTo(emptyMap<String, String>().toHeaders())
                }
        )
        assertThat(response.text).isEqualTo(text)
    }

    @Test
    fun `passes through query string to POST`()
    {
        val mockContext = mock<ActionContext> {
            on { this.queryString() } doReturn "key1=val1"
        }
        val client = getHttpClient()
        PorcelainAPIServer("Fake instance", "http://url-base", client)
                .post("/some/path/", mockContext)

        verify(client).newCall(
                check {
                    assertThat(it.url.toString()).isEqualTo("http://url-base/some/path/?key1=val1")
                    assertThat(it.headers).isEqualTo(standardHeaders.toHeaders())
                }
        )
    }

    @Test
    fun `makes DELETE call on http client`()
    {
        val mockContext = mock<ActionContext> {
            on { this.queryString() } doReturn "key1=val1"
        }
        val client = getHttpClient()
        PorcelainAPIServer("Fake instance", "http://url-base", client)
                .delete("/some/path/", mockContext)

        verify(client).newCall(
                check {
                    assertThat(it.url.toString()).isEqualTo("http://url-base/some/path/?key1=val1")
                    assertThat(it.headers).isEqualTo(standardHeaders.toHeaders())
                }
        )
    }

    @Test
    fun `throws error on failure with throwsOnError`()
    {
        val text = """{"status":"failure","errors":[{"error":"FOO","detail":"bar"}],"data":null}"""
        val client = getHttpClient(text, 500)
        val sut = PorcelainAPIServer("Fake instance", "http://url-base", client)
                .throwOnError()
        assertThatThrownBy { sut.get("/some/path/", context = mock()) }.isInstanceOf(PorcelainError::class.java)
        assertThatThrownBy { sut.post("/some/path/", mock()) }.isInstanceOf(PorcelainError::class.java)
        assertThatThrownBy { sut.delete("/some/path/", mock()) }.isInstanceOf(PorcelainError::class.java)
    }

    @Test
    fun `does not throw error on failure without throwsOnError`()
    {
        val client = getHttpClient(
                """{"status":"failure","errors":[{"error":"FOO","detail":"bar"}],"data":null}""",
                500
        )
        val response = PorcelainAPIServer("Fake instance", "http://url-base", client)
                .get("/some/path/", context = mock())
        assertThat(response.statusCode).isEqualTo(500)
        assertThat(response.text).isEqualTo(
                """{"data":null,"errors":[{"error":"FOO","message":"bar"}],"status":"failure"}"""
        )
    }

    @Test
    fun `passes through response json and status code`()
    {
        val json = """{"status": "success", "data": ["some data"], "errors": []}"""
        testTranslatedResponse(json, json)
    }

    @Test
    fun `translates response with null errors to empty array`()
    {
        val raw = """{"status": "success", "data": ["some data"], "errors": null}"""
        val expected = """{"status": "success", "data": ["some data"], "errors": []}"""
        testTranslatedResponse(raw, expected)
    }

    @Test
    fun `translates response error detail to error message`()
    {
        val raw = """{"status": "success", "data": ["some data"],
                           "errors": [{"code": "TEST1", "detail": "msg1"}, {"code": "TEST2", "detail": "msg2"}]}"""

        val expected = """{"status": "success", "data": ["some data"],
                            "errors": [{"code": "TEST1", "message": "msg1"}, {"code": "TEST2", "message": "msg2"}]}"""

        testTranslatedResponse(raw, expected)
    }

    @Test
    fun `translates response null error detail to empty string error message`()
    {
        val raw = """{"status": "success", "data": ["some data"],
                                  "errors": [{"code": "TEST1", "detail": "msg1"}, {"code": "TEST2", "detail": null}]}"""

        val expected = """{"status": "success", "data": ["some data"],
                                  "errors": [{"code": "TEST1", "message": "msg1"}, {"code": "TEST2", "message": ""}]}"""

        testTranslatedResponse(raw, expected)
    }

    @Test
    fun `translate ignores errors which are not objects`()
    {
        val raw = """{"status": "success", "data": ["some data"], "errors": ["err1", "err2"]}"""

        val expected = """{"status": "success", "data": ["some data"], "errors": []}"""

        testTranslatedResponse(raw, expected)
    }

    @Test
    fun `passes response errors through unchanged if no detail key`()
    {
        val json = """{"status": "success", "data": ["some data"],
                                  "errors": [{"code": "TEST1", "message": "msg1"}, {"code": "TEST2", "message": ""}]}"""
        testTranslatedResponse(json, json)
    }

    @Test
    fun `throws on failure if specified`()
    {
        val text = """{"status": "failure", "data": null, "errors": []}"""
        val client = getHttpClient(text, 400)

        val sut = PorcelainAPIServer("Fake instance", "http://url-base", client)

                .throwOnError()

        assertThatThrownBy { sut.get("/whatever", context = mock()) }
                .isInstanceOf(PorcelainError::class.java)
                .hasMessageContaining("Fake instance request failed for url /whatever")
                .matches { (it as PorcelainError).httpStatus == 400 }
    }

    @Test
    fun `throws on failure if specified for overloaded`()
    {
        val text = """{"status": "failure", "data": null, "errors": []}"""
        val client = getHttpClient(text, 400)

        val sut = PorcelainAPIServer("Fake instance", "http://url-base", client)

                .throwOnError()

        val key = "report-name"
        val queryParams: Map<String, String> = mapOf(key to "minimal").filter { it.key != key }
        assertThatThrownBy { sut.get("/whatever", queryParams) }
                .isInstanceOf(PorcelainError::class.java)
                .hasMessageContaining("Fake instance request failed for url /whatever")
                .matches { (it as PorcelainError).httpStatus == 400 }
    }

    @Test
    fun `can parse primitive data`()
    {
        val text = """{"status": "success", "data": 10, "errors": []}"""
        val response = PorcelainResponse(text, 200)
        val data = response.data(Int::class.java)
        assertThat(data).isEqualTo(10)
    }

    @Test
    fun `can parse object data and transform snake case to camel case`()
    {
        val text = """{"status": "success", "data": {"id": "12345", "date_time": "2019-03-29 16:25:48",
            |"age": 3600}, "errors": []}""".trimMargin()
        val response = PorcelainResponse(text, 200)
        val data = response.data(GitCommit::class.java)
        assertThat(data.dateTime).isEqualTo("2019-03-29 16:25:48")
        assertThat(data.age).isEqualTo(3600)
        assertThat(data.id).isEqualTo("12345")
    }

    @Test
    fun `can parse list data`()
    {
        val text = """{"status": "success", "data": [{"id": "12345", "date_time": "2019-03-29 16:25:48",
            |"age": 3600}], "errors": []}""".trimMargin()
        val response = PorcelainResponse(text, 200)
        val data = response.listData(GitCommit::class.java)
        assertThat(data[0].id).isEqualTo("12345")
        assertThat(data[0].dateTime).isEqualTo("2019-03-29 16:25:48")
        assertThat(data[0].age).isEqualTo(3600)
    }

    private fun testTranslatedResponse(rawResponse: String, expectedTranslatedResponse: String)
    {
        val mapper = ObjectMapper()
        val expectedJson = mapper.readTree(expectedTranslatedResponse)

        val sut1 = PorcelainAPIServer("Fake instance", "http://url-base",
                getHttpClient(rawResponse, 400))
        val getResult = sut1.get("anyUrl", context = mock())
        assertThat(mapper.readTree(getResult.text)).isEqualTo(expectedJson)
        assertThat(getResult.statusCode).isEqualTo(400)

        val sut2 = PorcelainAPIServer("Fake instance", "http://url-base",
                getHttpClient(rawResponse, 400))
        val postResult = sut2.post("anyUrl", mock())
        assertThat(mapper.readTree(postResult.text)).isEqualTo(expectedJson)
        assertThat(postResult.statusCode).isEqualTo(400)

        val sut3 = PorcelainAPIServer("Fake instance", "http://url-base",
                getHttpClient(rawResponse, 400))
        val deleteResult = sut3.delete("anyUrl", mock())
        assertThat(mapper.readTree(deleteResult.text)).isEqualTo(expectedJson)
        assertThat(deleteResult.statusCode).isEqualTo(400)
    }


    @Test
    fun `makes direct POST request`()
    {
        val client = getHttpClient()
        PorcelainAPIServer("Fake instance", "http://url-base", client).post(
                "/some/path",
                """{"key1": "val1"}""",
                mapOf(
                        "key2" to "val2"
                )
        )
        verify(client).newCall(
                check {
                    assertThat(it.url.toString()).isEqualTo("http://url-base/some/path?key2=val2")
                    assertThat(it.headers).isEqualTo(standardHeaders.toHeaders())
                    val buffer = Buffer()
                    it.body!!.writeTo(buffer)
                    assertThat(buffer.readUtf8()).isEqualTo("""{"key1": "val1"}""")
                }
        )
    }

    @Test
    fun `direct POST request passes through error`()
    {
        val rawResponse = """{"status":"failure","errors":[{"error":"FOO","detail":"bar"}],"data":null}"""
        val translatedResponse = """{"status":"failure","errors":[{"error":"FOO","message":"bar"}],"data":null}"""
        val response = PorcelainAPIServer("Fake instance", "http://url-base",
                getHttpClient(rawResponse, 400)).post("anyUrl", mock())
        assertThat(ObjectMapper().readTree(response.text)).isEqualTo(ObjectMapper().readTree(translatedResponse))
        assertThat(response.statusCode).isEqualTo(400)
    }

    @Test
    fun `direct POST request throws on error`()
    {
        val text = """{"status":"failure","errors":[{"error":"FOO","detail":"bar"}],"data":null}"""
        val client = getHttpClient(text, 500)
        val sut = PorcelainAPIServer("Fake instance", "http://url-base", client).throwOnError()
        assertThatThrownBy {
            sut.post(
                    "/some/path/",
                    "null",
                    emptyMap()
            )
        }.isInstanceOf(PorcelainError::class.java)
    }

    private fun getHttpClient(
            responseBody: String = """{"data": [], "errors": null, "status": "success"}""",
            responseCode: Int = 200
    ): OkHttpClient
    {
        val response = Response.Builder()
                .request(Request.Builder().url("http://url-base").build())
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

