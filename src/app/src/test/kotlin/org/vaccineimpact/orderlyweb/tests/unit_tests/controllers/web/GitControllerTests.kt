package org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.web

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.doThrow
import com.nhaarman.mockito_kotlin.mock
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

    // fun `fetch gets response from orderly`()
    // {
    //     val mockOrderlyServer = mock<OrderlyServerAPI>{
    //         on { it.post("/v1/reports/git/fetch/", mockContext) } doReturn mockResponse
    //     }

    //     val sut = GitController(mockContext, mockOrderlyServer)
    //     val response = sut.fetch()

    //     assertThat(response).isEqualTo("testResponse")
    // }

    @Test
    fun `fetch gets response from orderly, followed by git branches response`()
    {   
        // val fetchResponse = OrderlyServerResponse(Serializer.instance.toResult("fetchResponse"), 200)
        // val branchesResponse = OrderlyServerResponse(Serializer.instance.toResult("branchesResponse"), 200)

        val mockOrderlyServer = mock<OrderlyServerAPI>{
            on { it.post("/v1/reports/git/fetch/", mockContext) } doReturn
                OrderlyServerResponse(Serializer.instance.toResult("fetchResponse"), 200)
            on { it.get("/git/branches", mockContext) } doReturn
                OrderlyServerResponse(Serializer.instance.toResult("branchesResponse"), 200)
        }

        val sut = GitController(mockContext, mockOrderlyServer)
        // val response = sut.fetch()

        // assertThat(response).isEqualTo("branchesResponse")
    }

    @Test
    fun `fetch throws if orderly server returns an error`()
    {
        val mockContext = mock<ActionContext>()
        val mockOrderlyServerWithError = mock<OrderlyServerAPI> {
            on { post("/v1/reports/git/fetch/", mockContext) } doThrow OrderlyServerError("/v1/reports/git/fetch/", 400)
        }
        val mockOrderlyServer = mock<OrderlyServerAPI> {
            on { throwOnError() } doReturn mockOrderlyServerWithError
        }
        val sut = GitController(mockContext, mockOrderlyServer)
        assertThatThrownBy { sut.fetch() }
                .isInstanceOf(OrderlyServerError::class.java)
    }
}