package org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.web

import com.nhaarman.mockito_kotlin.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.pac4j.core.profile.CommonProfile
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.OrderlyServerAPI
import org.vaccineimpact.orderlyweb.OrderlyServerResponse
import org.vaccineimpact.orderlyweb.Serializer
import org.vaccineimpact.orderlyweb.controllers.web.ReportRunController
import org.vaccineimpact.orderlyweb.db.repositories.ReportRunRepository
import org.vaccineimpact.orderlyweb.db.repositories.WorkflowRunReportRepository
import org.vaccineimpact.orderlyweb.models.ReportRunLog
import org.vaccineimpact.orderlyweb.models.ReportRunWithDate
import org.vaccineimpact.orderlyweb.models.ReportStatus
import java.time.Instant

class ReportRunControllerTests
{
    private val startTime = Instant.now().epochSecond

    @Test
    fun `gets all running reports`()
    {
        val profile = mock<CommonProfile> {
            on { this.id } doReturn "test@test.com"
        }
        val context = mock<ActionContext> {
            on { this.userProfile } doReturn profile
        }
        val runningObject = ReportRunWithDate("name", "key", Instant.now())

        val repo = mock<ReportRunRepository> {
            on { this.getAllReportRunsForUser("test@test.com") } doReturn (listOf(runningObject))
        }

        val sut = ReportRunController(context, repo, mock(), mock())

        assertThat(sut.runningReports()).containsExactlyElementsOf(listOf(runningObject))
    }

    private val testReportRunLog = ReportRunLog(
            "user@text.com",
            Instant.now(),
            "test report",
            mapOf("source" to "dev"),
            mapOf("param1" to "value1"),
            "testBranch",
            "gitCommit",
            "running",
            "started running",
            null
    )

    private val testReportRunLogFinished = ReportRunLog(
            "user@text.com",
            Instant.now(),
            "test report",
            mapOf("source" to "dev"),
            mapOf("param1" to "value1"),
            "testBranch",
            "gitCommit",
            "success",
            "finished running",
            null
    )

    private val testOutput = listOf("started running", "still running")
    private val testReportStatus = ReportStatus(
            "testReportKey",
            "success",
            "tet report",
            "version1",
            testOutput,
            null,
            startTime
    )

    @Test
    fun `gets running report logs for non-workflow report which is still running`()
    {
        val context = mock<ActionContext> {
            on { this.params(":key") } doReturn "testReportKey"
            on { this.queryParams("workflow") } doReturn null as String?
        }

        val mockReportRepo = mock<ReportRunRepository> {
            on { this.getReportRun("testReportKey") } doReturn testReportRunLog
        }

        val mockWorkflowRepo = mock<WorkflowRunReportRepository>()

        val mockOrderlyServerResponse = OrderlyServerResponse(Serializer.instance.toResult(testReportStatus), 200)
        val mockAPI = mock<OrderlyServerAPI> {
            on { this.get(eq("/v1/reports/testReportKey/status/"), any<Map<String, String>>()) } doReturn mockOrderlyServerResponse
        }

        val sut = ReportRunController(context, mockReportRepo, mockWorkflowRepo, mockAPI)
        val result = sut.getRunningReportLogs()

        verify(mockWorkflowRepo, never()).checkReportIsInWorkflow(any(), any())
        verify(mockReportRepo).updateReportRun("testReportKey", "success", "version1", testOutput, Instant.ofEpochSecond(startTime))
        verify(mockReportRepo, times(2)).getReportRun("testReportKey")
        assertThat(result).isSameAs(testReportRunLog)
    }

    @Test
    fun `gets running report logs for non-workflow report which has finished running`()
    {
        val context = mock<ActionContext> {
            on { this.params(":key") } doReturn "testReportKey"
            on { this.queryParams("workflow") } doReturn null as String?
        }

        val mockReportRepo = mock<ReportRunRepository> {
            on { this.getReportRun("testReportKey") } doReturn testReportRunLogFinished
        }

        val mockWorkflowRepo = mock<WorkflowRunReportRepository>()

        val mockAPI = mock<OrderlyServerAPI>()

        val sut = ReportRunController(context, mockReportRepo, mockWorkflowRepo, mockAPI)
        val result = sut.getRunningReportLogs()

        verify(mockWorkflowRepo, never()).checkReportIsInWorkflow(any(), any())
        verify(mockReportRepo, never()).updateReportRun(any(), any(), any(), any(), any())
        verify(mockReportRepo, times(1)).getReportRun("testReportKey")
        verify(mockAPI, never()).get(any(), any<Map<String, String>>())
        assertThat(result).isSameAs(testReportRunLogFinished)
    }

    @Test
    fun `gets running report logs for workflow report which is still running`()
    {
        val context = mock<ActionContext> {
            on { this.params(":key") } doReturn "testReportKey"
            on { this.queryParams("workflow") } doReturn "testWorkflow"
        }

        val mockWorkflowRepo = mock<WorkflowRunReportRepository> {
            on { this.getReportRun("testReportKey") } doReturn testReportRunLog
        }

        val mockOrderlyServerResponse = OrderlyServerResponse(Serializer.instance.toResult(testReportStatus), 200)
        val mockAPI = mock<OrderlyServerAPI> {
            on { this.get(eq("/v1/reports/testReportKey/status/"), any<Map<String, String>>()) } doReturn mockOrderlyServerResponse
        }

        val sut = ReportRunController(context, mock(), mockWorkflowRepo, mockAPI)
        val result = sut.getRunningReportLogs()

        verify(mockWorkflowRepo).checkReportIsInWorkflow("testReportKey", "testWorkflow")
        verify(mockWorkflowRepo).updateReportRun("testReportKey", "success", "version1", testOutput, Instant.ofEpochSecond(startTime))
        verify(mockWorkflowRepo, times(2)).getReportRun("testReportKey")
        assertThat(result).isSameAs(testReportRunLog)
    }

    @Test
    fun `gets running report logs for workflow report which has finished running`()
    {
        val context = mock<ActionContext> {
            on { this.params(":key") } doReturn "testReportKey"
            on { this.queryParams("workflow") } doReturn "testWorkflow"
        }

        val mockWorkflowRepo = mock<WorkflowRunReportRepository> {
            on { this.getReportRun("testReportKey") } doReturn testReportRunLogFinished
        }

        val mockAPI = mock<OrderlyServerAPI>()

        val sut = ReportRunController(context, mock(), mockWorkflowRepo, mockAPI)
        val result = sut.getRunningReportLogs()

        verify(mockWorkflowRepo).checkReportIsInWorkflow("testReportKey", "testWorkflow")
        verify(mockWorkflowRepo, never()).updateReportRun(any(), any(), any(), any(), any())
        verify(mockAPI, never()).get(any(), any<Map<String, String>>())
        verify(mockWorkflowRepo, times(1)).getReportRun("testReportKey")
        assertThat(result).isSameAs(testReportRunLogFinished)
    }
}
