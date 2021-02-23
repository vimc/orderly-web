package org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.web.ReportControllerTests

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.doThrow
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThatThrownBy
import org.junit.Test
import org.vaccineimpact.orderlyweb.*
import org.vaccineimpact.orderlyweb.controllers.web.ReportController
import org.vaccineimpact.orderlyweb.db.repositories.ReportRepository
import org.vaccineimpact.orderlyweb.errors.OrderlyServerError
import org.vaccineimpact.orderlyweb.models.*
import java.time.Instant

class RunReportTests
{
    private val fakeBranchResponse = listOf(mapOf("name" to "master"), mapOf("name" to "dev"))
    private val fakeMetadata = RunReportMetadata(instancesSupported = true,
            gitSupported = true,
            instances = mapOf("source" to listOf("uat", "science")),
            changelogTypes = listOf("internal", "published"))

    @Test
    fun `getRunReport creates viewmodel`()
    {
        val mockContext = mock<ActionContext>()
        val mockOrderlyServerWithError = mock<OrderlyServerAPI> {
            on { get("/git/branches", mockContext) } doReturn
                    OrderlyServerResponse(Serializer.instance.toResult(fakeBranchResponse), 200)
            on { get("/run-metadata", mockContext) } doReturn
                    OrderlyServerResponse(Serializer.instance.toResult(fakeMetadata), 200)
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
        assertThat(result.runReportMetadata.instances["source"]).hasSameElementsAs(listOf("uat", "science"))
    }

    @Test
    fun `getRunReport returns no branches if git is not supported`()
    {
        val mockContext = mock<ActionContext>()
        val mockOrderlyServerWithError = mock<OrderlyServerAPI> {
            on { get("/run-metadata", mockContext) } doReturn
                    OrderlyServerResponse(Serializer.instance.toResult(fakeMetadata.copy(gitSupported = false)), 200)
            on { get("/git/branches", mockContext) } doReturn
                    OrderlyServerResponse(Serializer.instance.toResult(fakeBranchResponse), 200)
        }
        val mockOrderlyServer = mock<OrderlyServerAPI> {
            on { throwOnError() } doReturn mockOrderlyServerWithError
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
            on { get("/run-metadata", mockContext) } doThrow OrderlyServerError("/run-metadata", 400)
        }
        val mockOrderlyServer = mock<OrderlyServerAPI> {
            on { throwOnError() } doReturn mockOrderlyServerWithError
        }
        val sut = ReportController(mockContext, mock(), mockOrderlyServer, mock(), mock())
        assertThatThrownBy { sut.getRunReport() }
                .isInstanceOf(OrderlyServerError::class.java)
    }

    @Test
    fun `gets parameters for report as expected with commitId`()
    {
        val mockContext: ActionContext = mock {
            on { params(":name") } doReturn "minimal"
            on { queryParams("commit") } doReturn "123"
        }

        val parameters = listOf(
                Parameter("minimal", ""),
                Parameter("global", "default")
        )

        val mockOrderlyServer: OrderlyServerAPI = mock {
            on { get("/reports/minimal/parameters", mockContext) } doReturn
                    OrderlyServerResponse(Serializer.instance.toResult(parameters), 200)
        }

        val sut = ReportController(mockContext, mock(), mockOrderlyServer, mock(), mock())
        val result = sut.getReportParameters()
        Assertions.assertThat(result.count()).isEqualTo(2)
        Assertions.assertThat(result).isEqualTo(parameters)
    }

    @Test
    fun `gets parameters for report as expected without commitId`()
    {
        val mockContext: ActionContext = mock {
            on { params(":name") } doReturn "minimal"
        }

        val parameters = listOf(
                Parameter("minimal", ""),
                Parameter("global", "default")
        )

        val mockOrderlyServer: OrderlyServerAPI = mock {
            on { get("/reports/minimal/parameters", mockContext) } doReturn
                    OrderlyServerResponse(Serializer.instance.toResult(parameters), 200)
        }

        val sut = ReportController(mockContext, mock(), mockOrderlyServer, mock(), mock())
        val result = sut.getReportParameters()

        Assertions.assertThat(result.count()).isEqualTo(2)
        Assertions.assertThat(result).isEqualTo(parameters)
    }

    @Test
    fun `can getRunningReportsDetails`()
    {
        val fakeReportRunLog = ReportRunLog(
                "test@example.com",
                Instant.now(),
                "q123",
                "{‘0’: {‘source’: ‘support’, ‘annexe’: ‘annexe val’}}",
                "{‘0’: {‘name’: ‘cologne’, ‘value’: ‘memo’}}",
                "branch",
                "commit",
                "complete",
                "logs",
                "1233")

        val mockRepo = mock<ReportRepository> {
            on { getReportRun("fakeKey") } doReturn fakeReportRunLog
        }

        val mockContext: ActionContext = mock {
            on { params(":key") } doReturn "fakeKey"
        }

        val sut = ReportController(mockContext, mock(), mock(), mockRepo, mock())
        val result = sut.getRunningReportsDetails()
        assertThat(result).isEqualToComparingFieldByFieldRecursively(fakeReportRunLog)
    }
}
