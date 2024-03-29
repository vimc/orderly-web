package org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.web

import com.nhaarman.mockito_kotlin.*
import okhttp3.Headers
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.OrderlyServerAPI
import org.vaccineimpact.orderlyweb.PorcelainResponse
import org.vaccineimpact.orderlyweb.Serializer
import org.vaccineimpact.orderlyweb.controllers.web.GitController
import org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.api.ControllerTest

class GitControllerTests : ControllerTest()
{
    private val mockContext = mock<ActionContext>() {
            on { params(":branch") } doReturn "master"
        }
    private val mockResponse = PorcelainResponse("testResponse", 200, Headers.headersOf())
    @Test
    fun `gets commits for branch`()
    {
        val mockOrderlyServerAPI = mock<OrderlyServerAPI> {
            on { get("/git/commits?branch=master", mockContext) } doReturn
                    PorcelainResponse(Serializer.instance.toResult(listOf(1, 2, 3)), 200, Headers.headersOf())
        }
        val sut = GitController(mockContext, mockOrderlyServerAPI)
        val result = sut.getCommits()
        assertThat(result).isEqualTo(Serializer.instance.toResult(listOf(1, 2, 3)))
    }

    @Test
    fun `fetch receives successful fetch response from orderly, and returns git successful branches response`()
    {   
        val fetchResponse = PorcelainResponse(Serializer.instance.toResult("fetchResponse"), 200, Headers.headersOf())
        val branchesResponse = PorcelainResponse(Serializer.instance.toResult("branchesResponse"), 200, Headers.headersOf())

        val mockOrderlyServerAPI = mock<OrderlyServerAPI>{
            on { it.post("/v1/reports/git/fetch/", mockContext) } doReturn fetchResponse
            on { it.get("/git/branches", mockContext) } doReturn branchesResponse
        }

        val sut = GitController(mockContext, mockOrderlyServerAPI)
        val response = sut.fetch()

        assertThat(response).isEqualTo(Serializer.instance.toResult("branchesResponse"))
    }

    @Test
    fun `fetch receives and returns failed fetch response from orderly, and does not call git branches`()
    {   
        val mockContext2 = mock<ActionContext>()
        val fetchResponse = PorcelainResponse(Serializer.instance.toResult("fetchResponse"), 500, Headers.headersOf())
        val branchesResponse = PorcelainResponse(Serializer.instance.toResult("branchesResponse"), 200, Headers.headersOf())

        val mockOrderlyServerAPI = mock<OrderlyServerAPI>{
            on { it.post("/v1/reports/git/fetch/", mockContext2) } doReturn fetchResponse
            on { it.get("/git/branches", mockContext2) } doReturn branchesResponse
        }

        val sut = GitController(mockContext2, mockOrderlyServerAPI)
        val response = sut.fetch()

        assertThat(response).isEqualTo(Serializer.instance.toResult("fetchResponse"))
        verify(mockContext2).setStatusCode(500)
        verify(mockOrderlyServerAPI, times(0)).get(any(), context = any(), accept = eq("*/*"))
    }

    @Test
    fun `fetch receives successful fetch response but failed git branches response from orderly, and returns failed git branches response`()
    {   
        val mockContext2 = mock<ActionContext>()
        val fetchResponse = PorcelainResponse(Serializer.instance.toResult("fetchResponse"), 200, Headers.headersOf())
        val branchesResponse = PorcelainResponse(Serializer.instance.toResult("branchesResponse"), 500, Headers.headersOf())

        val mockOrderlyServerAPI = mock<OrderlyServerAPI>{
            on { it.post("/v1/reports/git/fetch/", mockContext2) } doReturn fetchResponse
            on { it.get("/git/branches", mockContext2) } doReturn branchesResponse
        }

        val sut = GitController(mockContext2, mockOrderlyServerAPI)
        val response = sut.fetch()
        
        assertThat(response).isEqualTo(Serializer.instance.toResult("branchesResponse"))
        verify(mockContext2).setStatusCode(500)
    }
}
