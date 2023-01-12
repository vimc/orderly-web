package org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.web.reportControllerTests

import com.nhaarman.mockito_kotlin.*
import org.assertj.core.api.Assertions
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThat
import org.assertj.core.api.AssertionsForInterfaceTypes.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.vaccineimpact.orderlyweb.*
import org.vaccineimpact.orderlyweb.controllers.web.ReportController
import org.vaccineimpact.orderlyweb.controllers.web.ReportRunController
import org.vaccineimpact.orderlyweb.db.repositories.ReportRunRepository
import org.vaccineimpact.orderlyweb.errors.PorcelainError
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
        val mockOrderlyServerAPIWithError = mock<OrderlyServerAPI> {
            on { get("/git/branches", queryParams) } doReturn
                    PorcelainResponse(Serializer.instance.toResult(fakeBranchResponse), 200, mock())
            on { get("/run-metadata", queryParams) } doReturn
                    PorcelainResponse(Serializer.instance.toResult(fakeMetadata), 200, mock())
        }
        val mockOrderlyServerAPI = mock<OrderlyServerAPI> {
            on { throwOnError() } doReturn mockOrderlyServerAPIWithError
        }
        val sut = ReportController(mock(), mock(), mockOrderlyServerAPI, mock(), mock())
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
        val mockOrderlyServerAPIWithError = mock<OrderlyServerAPI> {
            on { get("/run-metadata", mapOf()) } doReturn
                    PorcelainResponse(Serializer.instance.toResult(fakeMetadata.copy(gitSupported = false)), 200, mock())
            on { get("/git/branches", mapOf()) } doReturn
                    PorcelainResponse(Serializer.instance.toResult(fakeBranchResponse), 200, mock())
        }
        val mockOrderlyServerAPI = mock<OrderlyServerAPI> {
            on { throwOnError() } doReturn mockOrderlyServerAPIWithError
        }
        val sut = ReportController(mock(), mock(), mockOrderlyServerAPI, mock(), mock())
        val result = sut.getRunReport()

        assertThat(result.gitBranches).isEmpty()
    }

    @Test
    fun `getRunReport throws if orderly server returns an error`()
    {
        val mockOrderlyServerAPIWithError = mock<OrderlyServerAPI> {
            on { get("/run-metadata", mapOf()) } doThrow PorcelainError("/run-metadata", 400, "Orderly server")
        }
        val mockOrderlyServerAPI = mock<OrderlyServerAPI> {
            on { throwOnError() } doReturn mockOrderlyServerAPIWithError
        }
        val sut = ReportController(mock(), mock(), mockOrderlyServerAPI, mock(), mock())
        assertThatThrownBy { sut.getRunReport() }
                .isInstanceOf(PorcelainError::class.java)
    }

    @Test
    fun `getRunMetadata returns run report metadata`()
    {
        val mockOrderlyServerAPIWithError = mock<OrderlyServerAPI> {
            on { get("/git/branches", mapOf()) } doReturn
                    PorcelainResponse(Serializer.instance.toResult(fakeBranchResponse), 200, mock())
            on { get("/run-metadata", mapOf()) } doReturn
                    PorcelainResponse(Serializer.instance.toResult(fakeMetadata), 200, mock())
        }
        val mockOrderlyServerAPI = mock<OrderlyServerAPI> {
            on { throwOnError() } doReturn mockOrderlyServerAPIWithError
        }

        val sut = ReportController(mock(), mock(), mockOrderlyServerAPI, mock(), mock())
        val result = sut.getRunMetadata()
        assertThat(result.gitBranches).hasSameElementsAs(listOf("master", "dev"))
        assertThat(result.metadata.gitSupported).isTrue()
        assertThat(result.metadata.instancesSupported).isTrue()
        assertThat(result.metadata.changelogTypes).hasSameElementsAs(listOf("internal", "published"))
        assertThat(result.metadata.instances["source"]).hasSameElementsAs(listOf("uat", "science"))
    }

    @Test
    fun `gets parameters for report as expected with commitId`()
    {
        val mockQueryParameters = mapOf("commit" to "123")
        val mockContext: ActionContext = mock {
            on { params(":name") } doReturn "minimal"
            on { queryParams() } doReturn mockQueryParameters
        }

        val parameters = listOf(
                Parameter("minimal", ""),
                Parameter("global", "default")
        )

        val mockOrderlyServerAPI: OrderlyServerAPI = mock {
            on { getReportParameters("minimal", mockQueryParameters) } doReturn parameters
        }

        val sut = ReportController(mockContext, mock(), mockOrderlyServerAPI, mock(), mock())
        val result = sut.getReportParameters()
        Assertions.assertThat(result.count()).isEqualTo(2)
        Assertions.assertThat(result).isEqualTo(parameters)
    }

    @Test
    fun `gets parameters for report as expected without commitId`()
    {
        val mockQueryParameters: Map<String, String> = mapOf()
        val mockContext: ActionContext = mock {
            on { params(":name") } doReturn "minimal"
            on { queryParams() } doReturn mockQueryParameters
        }

        val parameters = listOf(
                Parameter("minimal", ""),
                Parameter("global", "default")
        )

        val mockOrderlyServerAPI: OrderlyServerAPI = mock {
            on { getReportParameters("minimal", mockQueryParameters) } doReturn parameters
        }

        val sut = ReportController(mockContext, mock(), mockOrderlyServerAPI, mock(), mock())
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

        val sut = ReportRunController(mockContext, mockRepo, mock(), mock())
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
      "queue": [],
      "start_time": "1647340549"
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
        assertThat(status.startTime).isEqualTo(1647340549L)
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

        val mockOrderlyResponse = PorcelainResponse("""{"data": $testStatusJson}""",200, mock())

        val mockAPI = mock<OrderlyServerAPI> {
            on { get("/v1/reports/fakeKey/status/", mapOf("output" to "true")) } doReturn  mockOrderlyResponse
        }

        val sut = ReportRunController(mockContext, mockRepo, mock(), mockAPI)
        val result = sut.getRunningReportLogs()
        verify(mockRepo, times(2)).getReportRun("fakeKey")
        assertThat(result).isSameAs(incompleteLog)
        verify(mockRepo).updateReportRun(
            eq("fakeKey"),
            eq("updatedStatus"),
            eq("1233"),
            eq(listOf("output item")),
            eq(Instant.ofEpochSecond(1647340549L)))
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

        val sut = ReportRunController(mockContext, mockRepo, mock(), mock())
        Assertions.assertThatThrownBy { sut.getRunningReportLogs() }
                .isInstanceOf(UnknownObjectError::class.java)
    }
}
