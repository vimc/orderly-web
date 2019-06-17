package org.vaccineimpact.orderlyweb.tests.security

import com.nhaarman.mockito_kotlin.*
import org.junit.Test
import org.vaccineimpact.orderlyweb.security.AllowedOriginsFilter
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests
import spark.Request
import spark.Response
import javax.servlet.http.HttpServletResponse

class AllowedOriginsFilterTests: TeamcityTests()
{
    @Test
    fun `adds header if origin matches allowed origin`()
    {
        val request = mock<Request>{
            on { headers("Origin") } doReturn "https://montagu.vaccineimpact.org/"
        }

        val rawResponse = mock<HttpServletResponse>()

        val response = mock<Response>{
            on { it.raw() } doReturn rawResponse
        }

        val sut = AllowedOriginsFilter(false)
        sut.handle(request, response)

        assertHeaderWasAdded(rawResponse,"https://montagu.vaccineimpact.org/")
    }

    @Test
    fun `does not adds header if origin does not match allowed origin`()
    {
        val request = mock<Request>{
            on { headers("Origin") } doReturn "https://somewhereelse.vaccineimpact.org/"
        }

        val rawResponse = mock<HttpServletResponse>()

        val response = mock<Response>{
            on { it.raw() } doReturn rawResponse
        }

        val sut = AllowedOriginsFilter(false)
        sut.handle(request, response)

        assertHeaderWasNotAdded(rawResponse)
    }

    @Test
    fun `adds header if origin matches localhost and localhost is allowed`()
    {
        val request = mock<Request>{
            on { headers("Origin") } doReturn "https://localhost"
        }

        val rawResponse = mock<HttpServletResponse>()

        val response = mock<Response>{
            on { it.raw() } doReturn rawResponse
        }

        val sut = AllowedOriginsFilter(true)
        sut.handle(request, response)

        assertHeaderWasAdded(rawResponse,"https://localhost")
    }

    @Test
    fun `does not add header if origin matches localhost and localhost is not allowed`()
    {
        val request = mock<Request>{
            on { headers("Origin") } doReturn "https://localhost"
        }

        val rawResponse = mock<HttpServletResponse>()

        val response = mock<Response>{
            on { it.raw() } doReturn rawResponse
        }

        val sut = AllowedOriginsFilter(false)
        sut.handle(request, response)

        assertHeaderWasNotAdded(rawResponse)
    }

    @Test
    fun `does not add header if origin is not set`()
    {
        val request = mock<Request>{
            on { headers("Origin") } doReturn ""
        }

        val rawResponse = mock<HttpServletResponse>()

        val response = mock<Response>{
            on { it.raw() } doReturn rawResponse
        }

        val sut = AllowedOriginsFilter(true)
        sut.handle(request, response)

        assertHeaderWasNotAdded(rawResponse)
    }

    private fun assertHeaderWasAdded(mockResponse: HttpServletResponse, expectedHeaderValue: String)
    {
        verify(mockResponse).addHeader("Access-Control-Allow-Origin", expectedHeaderValue)
    }

    private fun assertHeaderWasNotAdded(mockResponse: HttpServletResponse)
    {
        verify(mockResponse, never()).addHeader(any(), any())
    }
}