package org.vaccineimpact.reporting_api.tests.unit_tests

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.junit.Test
import org.vaccineimpact.reporting_api.ActionContext
import org.vaccineimpact.reporting_api.ContentTypes
import org.vaccineimpact.reporting_api.HttpClient
import org.vaccineimpact.reporting_api.OrderlyServer
import org.vaccineimpact.reporting_api.db.ConfigWrapper

class OrderlyServerTests
{
    private val mockHttpclient = mock<HttpClient>()
    private val mockConfig = mock<ConfigWrapper>(){
        on {this.get("orderly.server")} doReturn "http://orderly"
    }

    private val standardHeaders = mapOf(
            "Accept" to ContentTypes.json,
            "Accept-Encoding" to "gzip"
    )

    @Test
    fun `passes through POST body`()
    {
        val mockContext = mock<ActionContext>() {
            on { this.postData() } doReturn mapOf("key1" to "val1")
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