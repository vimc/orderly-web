package org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.web.ReportControllerTests

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.doThrow
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThatThrownBy
import org.junit.Test
import org.vaccineimpact.orderlyweb.*
import org.vaccineimpact.orderlyweb.controllers.web.ReportController
import org.vaccineimpact.orderlyweb.errors.OrderlyServerError
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests

class RunReportTests : TeamcityTests()
{
    private val fakeBranchResponse = listOf(mapOf("name" to "master"), mapOf("name" to "dev"))

    @Test
    fun `getRunReport creates viewmodel`()
    {
        val mockContext = mock<ActionContext>()
        val mockOrderlyServerWithError = mock<OrderlyServerAPI> {
            on { get("/git/branches", mockContext) } doReturn
                    OrderlyServerResponse(Serializer.instance.toResult(fakeBranchResponse), 200)
        }
        val mockOrderlyServer = mock<OrderlyServerAPI> {
            on { throwOnError() } doReturn mockOrderlyServerWithError
        }
        val sut = ReportController(mockContext, mock(), mockOrderlyServer, mock(), mock())
        val result = sut.getRunReport()

        assertThat(result.breadcrumbs.count()).isEqualTo(2)
        assertThat(result.breadcrumbs[1].name).isEqualTo("Run a report")
        assertThat(result.breadcrumbs[1].url).isEqualTo("http://localhost:8888/run-report")
        assertThat(result.gitBranches).hasSameElementsAs(listOf("master", "dev"))
        assertThat(result.runReportMetadata.gitSupported).isTrue()
        assertThat(result.runReportMetadata.instancesSupported).isTrue()
        assertThat(result.runReportMetadata.changelogTypes).hasSameElementsAs(listOf("internal", "published"))
    }

    @Test
    fun `getRunReport returns no branches if orderly server returns an error`()
    {
        val mockContext = mock<ActionContext>()
        val mockOrderlyServer = mock<OrderlyServerAPI> {
            on { get("/git/branches", mockContext) } doReturn
                    OrderlyServerResponse(Serializer.instance.toResult(null), 500)
        }
        val sut = ReportController(mockContext, mock(), mockOrderlyServer, mock(), mock())
        val result = sut.getRunReport()

        assertThat(result.gitBranches).isEmpty()
    }

    @Test
    fun `getRunReport returns no branches if orderly server response is malformed`()
    {
        val mockContext = mock<ActionContext>()
        val mockOrderlyServer = mock<OrderlyServerAPI> {
            on { get("/git/branches", mockContext) } doReturn
                    OrderlyServerResponse(Serializer.instance.toResult(listOf(mapOf("something" to "something"))), 200)
        }
        val sut = ReportController(mockContext, mock(), mockOrderlyServer, mock(), mock())
        val result = sut.getRunReport()

        assertThat(result.gitBranches).isEmpty()
    }

    @Test
    fun `getRunReport throws if orderly server returns an error`()
    {
        val mockContext = mock<ActionContext>()
        val mockOrderlyServerWithError = mock<OrderlyServerAPI> {
            on { get("/git/branches", mockContext) } doThrow OrderlyServerError("/git/branches", 400)
        }
        val mockOrderlyServer = mock<OrderlyServerAPI> {
            on { throwOnError() } doReturn mockOrderlyServerWithError
        }
        val sut = ReportController(mockContext, mock(), mockOrderlyServer, mock(), mock())
        assertThatThrownBy { sut.getRunReport() }
                .isInstanceOf(OrderlyServerError::class.java)
    }

}
