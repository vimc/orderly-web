package org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.api

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.OrderlyServerAPI
import org.vaccineimpact.orderlyweb.OrderlyServerResponse
import org.vaccineimpact.orderlyweb.controllers.api.QueueController

class QueueControllerTests : ControllerTest()
{
    @Test
    fun `getStatus fetches queue status from orderly server`()
    {
        val mockContext = mock<ActionContext>()
        val mockResponse = OrderlyServerResponse("testResponse", 200)
        val mockOrderly = mock<OrderlyServerAPI> {
            on { it.get("/v1/queue/status/", mockContext) } doReturn mockResponse
        }

        val sut = QueueController(mockContext, mock(), mockOrderly)
        val response = sut.getStatus()
        assertThat(response).isEqualTo("testResponse")
    }
}
