package org.vaccineimpact.orderlyweb.tests.unit_tests

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import khttp.responses.Response
import org.json.JSONObject
import org.junit.Test
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.ContentTypes
import org.vaccineimpact.orderlyweb.HttpClient
import org.vaccineimpact.orderlyweb.OrderlyServer
import org.vaccineimpact.orderlyweb.db.Config
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests

class OrderlyServerTests: TeamcityTests()
{

    private val mockResponse = mock<Response> {
        on {this.jsonObject} doReturn JSONObject()
    }

    private val mockHttpclient = mock<HttpClient> {
        on {this.get(any(), any())} doReturn mockResponse
        on {this.post(any(), any(), any())} doReturn mockResponse
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

        verify(mockHttpclient).post("http://orderly/v1/some/path/", standardHeaders, mapOf("key1" to "val1"))
    }

    @Test
    fun `passes through query string to POST`()
    {
        val mockContext = mock<ActionContext>() {
            on { this.queryString() } doReturn "key1=val1"
        }
        val sut = OrderlyServer(mockConfig, mockHttpclient)
        sut.post("/some/path/", mockContext)

        verify(mockHttpclient).post("http://orderly/v1/some/path/?key1=val1", standardHeaders, mapOf())
    }

    @Test
    fun `passes through query string to GET`()
    {
        val mockContext = mock<ActionContext>() {
            on { this.queryString() } doReturn "key1=val1"
        }
        val sut = OrderlyServer(mockConfig, mockHttpclient)
        sut.get("/some/path/", mockContext)

        verify(mockHttpclient).get("http://orderly/v1/some/path/?key1=val1", standardHeaders)
    }
}