package org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.web.reportControllerTests

import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.OrderlyServerAPI
import org.vaccineimpact.orderlyweb.OrderlyServerResponse
import org.vaccineimpact.orderlyweb.controllers.web.ReportController

class MetadataTests
{
    @Test
    fun `can get dependencies`()
    {
        val mockContext = mock<ActionContext>{
            on { params(":name") } doReturn "testName"
        }
        val mockOrderlyResponse = OrderlyServerResponse("testResponse", 200)
        val mockOrderlyServer = mock<OrderlyServerAPI>{
            on { get("/v1/reports/testName/dependencies/", mockContext) } doReturn mockOrderlyResponse
        }
        val sut = ReportController(mockContext, mock(), mockOrderlyServer, mock(), mock())
        val result = sut.getDependencies()
        assertThat(result).isEqualTo("testResponse")
        verify(mockContext).setStatusCode(200)
    }
}
