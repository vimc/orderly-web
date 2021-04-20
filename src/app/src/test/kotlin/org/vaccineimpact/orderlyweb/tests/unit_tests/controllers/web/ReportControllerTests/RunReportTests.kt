package org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.web.ReportControllerTests

import com.nhaarman.mockito_kotlin.*
import org.assertj.core.api.Assertions
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThatThrownBy
import org.junit.Test
import org.vaccineimpact.orderlyweb.*
import org.vaccineimpact.orderlyweb.controllers.web.ReportController
import org.vaccineimpact.orderlyweb.controllers.web.ReportRunController
import org.vaccineimpact.orderlyweb.db.repositories.ReportRunRepository
import org.vaccineimpact.orderlyweb.errors.OrderlyServerError
import org.vaccineimpact.orderlyweb.errors.UnknownObjectError
import org.vaccineimpact.orderlyweb.models.*
import java.time.Instant

class RunReportTests
{
    private val fakeBranchResponse = listOf(mapOf("name" to "master"), mapOf("name" to "dev"))
    private val fakeMetadata = RunReportMetadata(instancesSupported = true,
            gitSupported = true,
            instances = mapOf("source" to listOf("uat", "science")),
            changelogTypes = listOf("internal", "published"))

    private val instant = Instant.now()
    private val fakeReportRunLog = ReportRunLog(
            "test@example.com",
            instant,
            "q123",
            mapOf("annex" to "production", "source" to "production"),
            mapOf("name" to "cologne", "value" to "memo"),
            "branch",
            "commit",
            "complete",
            "logs",
            "1233")

    @Test
    fun `getRunReport creates viewmodel`()
    {
        val key = "report-key"
        val queryParams: Map<String, String> = mapOf(key to "minimal").filter { it.key != key }
        val mockOrderlyServerWithError = mock<OrderlyServerAPI> {
            on { get("/git/branches", queryParams) } doReturn
                    OrderlyServerResponse(Serializer.instance.toResult(fakeBranchResponse), 200)
            on { get("/run-metadata", queryParams) } doReturn
                    OrderlyServerResponse(Serializer.instance.toResult(fakeMetadata), 200)
        }
        val mockOrderlyServer = mock<OrderlyServerAPI> {
            on { throwOnError() } doReturn mockOrderlyServerWithError
        }
        val sut = ReportController(mock(), mock(), mockOrderlyServer, mock(), mock())
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
        val mockOrderlyServerWithError = mock<OrderlyServerAPI> {
            on { get("/run-metadata", mapOf()) } doReturn
                    OrderlyServerResponse(Serializer.instance.toResult(fakeMetadata.copy(gitSupported = false)), 200)
            on { get("/git/branches", mapOf()) } doReturn
                    OrderlyServerResponse(Serializer.instance.toResult(fakeBranchResponse), 200)
        }
        val mockOrderlyServer = mock<OrderlyServerAPI> {
            on { throwOnError() } doReturn mockOrderlyServerWithError
        }
        val sut = ReportController(mock(), mock(), mockOrderlyServer, mock(), mock())
        val result = sut.getRunReport()

        assertThat(result.gitBranches).isEmpty()
    }

    @Test
    fun `getRunReport throws if orderly server returns an error`()
    {
        val mockOrderlyServerWithError = mock<OrderlyServerAPI> {
            on { get("/run-metadata", mapOf()) } doThrow OrderlyServerError("/run-metadata", 400)
        }
        val mockOrderlyServer = mock<OrderlyServerAPI> {
            on { throwOnError() } doReturn mockOrderlyServerWithError
        }
        val sut = ReportController(mock(), mock(), mockOrderlyServer, mock(), mock())
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
    fun `can getRunningReportLogs`()
    {
        val mockRepo = mock<ReportRunRepository> {
            on { getReportRun("fakeKey") } doReturn fakeReportRunLog
        }

        val mockContext: ActionContext = mock {
            on { params(":key") } doReturn "fakeKey"
        }

        val sut = ReportRunController(mockContext, mockRepo, mock())
        val result = sut.getRunningReportLogs()
        verify(mockRepo, times(1)).getReportRun("fakeKey")
        assertThat(result.email).isEqualTo("test@example.com")
        Assertions.assertThat(result.date).isEqualTo(instant)
        Assertions.assertThat(result.report).isEqualTo("q123")
        Assertions.assertThat(result.instances).isEqualTo(mapOf("annex" to "production", "source" to "production"))
        Assertions.assertThat(result.params).isEqualTo(mapOf("name" to "cologne", "value" to "memo"))
        Assertions.assertThat(result.gitBranch).isEqualTo("branch")
        Assertions.assertThat(result.gitCommit).isEqualTo("commit")
        Assertions.assertThat(result.status).isEqualTo("complete")
        Assertions.assertThat(result.logs).isEqualTo("logs")
        Assertions.assertThat(result.reportVersion).isEqualTo("1233")
    }

    private val testStatusJson = """{
      "key": "fakeKey", 
      "status": "updatedStatus",
      "name": "test", 
      "version": "1233", 
      "output": ["output item"], 
      "queue": []
     }"""

    @Test
    fun `can deserialise ReportStatus`() {
        val status = Serializer.instance.gson.fromJson(testStatusJson, ReportStatus::class.java)
        assertThat(status.key).isEqualTo("fakeKey")
        assertThat(status.status).isEqualTo("updatedStatus")
        assertThat(status.name).isEqualTo("test")
        assertThat(status.version).isEqualTo("1233")
        assertThat(status.output).isEqualTo(listOf("output item"))
        assertThat(status.queue).isEqualTo(listOf<Any>())
    }

    private fun testRunningLogRefresh(incompleteStatus: String?)
    {
        val incompleteLog = fakeReportRunLog.copy(status = incompleteStatus)

        val mockRepo = mock<ReportRunRepository> {
            on { getReportRun("fakeKey") } doReturn incompleteLog
        }

        val mockContext = mock<ActionContext> {
            on { params(":key") } doReturn "fakeKey"
        }

        val mockOrderlyResponse = OrderlyServerResponse("""{"data": $testStatusJson}""",200)

        val mockAPI = mock<OrderlyServerAPI> {
            on { get("/v1/reports/fakeKey/status/", mapOf("output" to "true")) } doReturn  mockOrderlyResponse
        }

        val sut = ReportRunController(mockContext, mockRepo, mockAPI)
        val result = sut.getRunningReportLogs()
        verify(mockRepo, times(2)).getReportRun("fakeKey")
        assertThat(result).isSameAs(incompleteLog)
        verify(mockRepo).updateReportRun(eq("fakeKey"), eq("updatedStatus"), eq("1233"), eq(listOf("output item")))
    }

    @Test
    fun `getRunningReportLogs refreshes from orderly server when report status is null`()
    {
        testRunningLogRefresh(null)
    }

    @Test
    fun `getRunningReportLogs refreshes from orderly server when report status is queued`()
    {
        testRunningLogRefresh("queued")
    }

    @Test
    fun `getRunningReportLogs refreshes from orderly server when report status is running`()
    {
        testRunningLogRefresh("running")
    }

    @Test
    fun `running report logs can throw unknown exception`()
    {
        val mockContext = mock<ActionContext> {
            on { this.params(":key") } doReturn "fakeKey"
        }

        val mockRepo = mock<ReportRunRepository> {
            on { getReportRun("fakeKey") } doThrow UnknownObjectError("key", "getReportRun")
        }

        val sut = ReportRunController(mockContext, mockRepo, mock())
        Assertions.assertThatThrownBy { sut.getRunningReportLogs() }
                .isInstanceOf(UnknownObjectError::class.java)
    }
}
