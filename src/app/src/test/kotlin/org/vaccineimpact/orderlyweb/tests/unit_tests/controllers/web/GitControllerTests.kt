package org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.web

import com.nhaarman.mockito_kotlin.*
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThatThrownBy
import org.junit.Test
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.OrderlyServerAPI
import org.vaccineimpact.orderlyweb.OrderlyServerResponse
import org.vaccineimpact.orderlyweb.errors.OrderlyServerError
import org.vaccineimpact.orderlyweb.Serializer
import org.vaccineimpact.orderlyweb.controllers.web.GitController
import org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.api.ControllerTest

class GitControllerTests : ControllerTest()
{
    private val mockContext = mock<ActionContext>() {
            on { params(":branch") } doReturn "master"
        }
    private val mockResponse = OrderlyServerResponse("testResponse", 200)
    @Test
    fun `gets commits for branch`()
    {
        val mockOrderlyServer = mock<OrderlyServerAPI> {
            on { get("/git/commits?branch=master", mockContext) } doReturn
                    OrderlyServerResponse(Serializer.instance.toResult(listOf(1, 2, 3)), 200)
        }
        val sut = GitController(mockContext, mockOrderlyServer)
        val result = sut.getCommits()
        assertThat(result).isEqualTo(Serializer.instance.toResult(listOf(1, 2, 3)))
    }

    @Test
    fun `fetch receives successful fetch response from orderly, and returns git successful branches response`()
    {   
        val fetchResponse = OrderlyServerResponse(Serializer.instance.toResult("fetchResponse"), 200)
        val branchesResponse = OrderlyServerResponse(Serializer.instance.toResult("branchesResponse"), 200)

        val mockOrderlyServer = mock<OrderlyServerAPI>{
            on { it.post("/v1/reports/git/fetch/", mockContext) } doReturn fetchResponse
            on { it.get("/git/branches", mockContext) } doReturn branchesResponse
        }

        val sut = GitController(mockContext, mockOrderlyServer)
        val response = sut.fetch()

        assertThat(response).isEqualTo(Serializer.instance.toResult("branchesResponse"))
    }

    @Test
    fun `fetch receives and returns failed fetch response from orderly, and does not call git branches`()
    {   
        val mockContext2 = mock<ActionContext>()
        val fetchResponse = OrderlyServerResponse(Serializer.instance.toResult("fetchResponse"), 500)
        val branchesResponse = OrderlyServerResponse(Serializer.instance.toResult("branchesResponse"), 200)

        val mockOrderlyServer = mock<OrderlyServerAPI>{
            on { it.post("/v1/reports/git/fetch/", mockContext2) } doReturn fetchResponse
            on { it.get("/git/branches", mockContext2) } doReturn branchesResponse
        }

        val sut = GitController(mockContext2, mockOrderlyServer)
        val response = sut.fetch()

        assertThat(response).isEqualTo(Serializer.instance.toResult("fetchResponse"))
        verify(mockContext2).setStatusCode(500)
    }

    @Test
    fun `fetch receives successful fetch response but failed git branches response from orderly, and returns failed git branches response`()
    {   
        val mockContext2 = mock<ActionContext>()
        val fetchResponse = OrderlyServerResponse(Serializer.instance.toResult("fetchResponse"), 200)
        val branchesResponse = OrderlyServerResponse(Serializer.instance.toResult("branchesResponse"), 500)

        val mockOrderlyServer = mock<OrderlyServerAPI>{
            on { it.post("/v1/reports/git/fetch/", mockContext2) } doReturn fetchResponse
            on { it.get("/git/branches", mockContext2) } doReturn branchesResponse
        }

        val sut = GitController(mockContext2, mockOrderlyServer)
        val response = sut.fetch()
        
        assertThat(response).isEqualTo(Serializer.instance.toResult("branchesResponse"))
        verify(mockContext2).setStatusCode(500)
    }
}