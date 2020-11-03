package org.vaccineimpact.orderlyweb.tests.unit_tests

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.JsonParseException
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import khttp.responses.Response
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.json.JSONObject
import org.junit.Test
import org.vaccineimpact.orderlyweb.*
import org.vaccineimpact.orderlyweb.db.Config
import org.vaccineimpact.orderlyweb.errors.OrderlyServerError
import org.vaccineimpact.orderlyweb.models.GitCommit
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests

class OrderlyServerTests : TeamcityTests()
{

    private val mockResponse = mock<Response> {
        on { this.jsonObject } doReturn JSONObject()
    }

    private val mockHttpclient = mock<HttpClient> {
        on { this.get(any(), any()) } doReturn mockResponse
        on { this.post(any(), any(), any()) } doReturn mockResponse
        on { this.delete(any(), any()) } doReturn mockResponse
    }
    private val mockConfig = mock<Config>() {
        on { this.get("orderly.server") } doReturn "http://orderly"
    }

    private val standardHeaders = mapOf(
            "Accept" to ContentTypes.json,
            "Accept-Encoding" to "gzip"
    )

    @Test
    fun `passes through POST body`()
    {
        val mockContext = mock<ActionContext>() {
            on { this.postData<String>() } doReturn mapOf("key1" to "val1")
        }
        val sut = OrderlyServer(mockConfig, mockHttpclient)
        sut.post("/some/path/", mockContext)

        verify(mockHttpclient).post("http://orderly/some/path/", standardHeaders, mapOf("key1" to "val1"))
    }

    @Test
    fun `passes through query string to POST`()
    {
        val mockContext = mock<ActionContext>() {
            on { this.queryString() } doReturn "key1=val1"
        }
        val sut = OrderlyServer(mockConfig, mockHttpclient)
        sut.post("/some/path/", mockContext)

        verify(mockHttpclient).post("http://orderly/some/path/?key1=val1", standardHeaders, mapOf())
    }

    @Test
    fun `passes through query string to GET`()
    {
        val mockContext = mock<ActionContext>() {
            on { this.queryString() } doReturn "key1=val1"
        }
        val sut = OrderlyServer(mockConfig, mockHttpclient)
        sut.get("/some/path/", mockContext)

        verify(mockHttpclient).get("http://orderly/some/path/?key1=val1", standardHeaders)
    }

    @Test
    fun `makes DELETE call on http client`()
    {
        val mockContext = mock<ActionContext>() {
            on { this.queryString() } doReturn "key1=val1"
        }
        val sut = OrderlyServer(mockConfig, mockHttpclient)
        sut.delete("/some/path/", mockContext)

        verify(mockHttpclient).delete("http://orderly/some/path/?key1=val1", standardHeaders)
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
        val rawJson = JSONObject(text)
        val mockRawResponse = mock<Response> {
            on { this.jsonObject } doReturn rawJson
            on { this.statusCode } doReturn 400
        }

        val mockClient = mock<HttpClient> {
            on { this.get(any(), any()) } doReturn mockRawResponse
        }

        val sut = OrderlyServer(mockConfig, mockClient)
                .throwOnError()

        assertThatThrownBy { sut.get("/whatever", mock()) }
                .isInstanceOf(OrderlyServerError::class.java)
                .hasMessageContaining("Orderly server request failed for url /whatever")
                .matches { (it as OrderlyServerError).httpStatus == 400 }
    }

    @Test
    fun `can parse primitive data`()
    {
        val text = """{"status": "success", "data": 10, "errors": []}"""
        val response = OrderlyServerResponse(text, 200)
        val data = response.data(Int::class.java)
        assertThat(data).isEqualTo(10)
    }

    @Test
    fun `can parse object data and transform snake case to camel case`()
    {
        val text = """{"status": "success", "data": {"id": "12345", "date_time": "2019-03-29 16:25:48", 
            |"display_date_time": "Monday"}, "errors": []}""".trimMargin()
        val response = OrderlyServerResponse(text, 200)
        val data = response.data(GitCommit::class.java)
        assertThat(data.dateTime).isEqualTo("2019-03-29 16:25:48")
        assertThat(data.displayDateTime).isEqualTo("Monday")
        assertThat(data.id).isEqualTo("12345")
    }

    @Test
    fun `can parse list data`()
    {
        val text = """{"status": "success", "data": [{"id": "12345", "date_time": "2019-03-29 16:25:48", 
            |"display_date_time": "Monday"}], "errors": []}""".trimMargin()
        val response = OrderlyServerResponse(text, 200)
        val data = response.listData(GitCommit::class.java)
        assertThat(data[0].id).isEqualTo("12345")
        assertThat(data[0].dateTime).isEqualTo("2019-03-29 16:25:48")
        assertThat(data[0].displayDateTime).isEqualTo("Monday")
    }

    private fun testTranslatedResponse(rawResponse: String, expectedTranslatedResponse: String)
    {
        val rawJson = JSONObject(rawResponse)
        val mockRawResponse = mock<Response> {
            on { this.jsonObject } doReturn rawJson
            on { this.statusCode } doReturn 400
        }
        val mockClient = mock<HttpClient> {
            on { this.get(any(), any()) } doReturn mockRawResponse
            on { this.post(any(), any(), any()) } doReturn mockRawResponse
        }
        val mapper = ObjectMapper()
        val expectedJson = mapper.readTree(expectedTranslatedResponse)


        val sut = OrderlyServer(mockConfig, mockClient)

        val getResult = sut.get("anyUrl", mock())
        assertThat(mapper.readTree(getResult.text)).isEqualTo(expectedJson)
        assertThat(getResult.statusCode).isEqualTo(400)

        val postResult = sut.get("anyUrl", mock())
        assertThat(mapper.readTree(postResult.text).equals(expectedJson)).isTrue()
        assertThat(postResult.statusCode).isEqualTo(400)

        val deleteResult = sut.delete("anyUrl", mock())
        assertThat(mapper.readTree(postResult.text).equals(expectedJson)).isTrue()
        assertThat(postResult.statusCode).isEqualTo(400)
    }
}

