package org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.web.ReportControllerTests

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.doThrow
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.web.WorkflowRunController
import org.vaccineimpact.orderlyweb.db.repositories.WorkflowRunRepository
import org.vaccineimpact.orderlyweb.errors.UnknownObjectError
import org.vaccineimpact.orderlyweb.models.WorkflowReportWithParams
import org.vaccineimpact.orderlyweb.models.WorkflowRun
import java.time.Instant
import org.vaccineimpact.orderlyweb.viewmodels.Breadcrumb
import org.vaccineimpact.orderlyweb.viewmodels.IndexViewModel

class WorkflowRunControllerTests
{
    @Test
    fun `can get get workflow details`()
    {
        val now = Instant.now()
        val workflowRun = WorkflowRun(
                "Interim report",
                "adventurous_aardvark",
                "user@email.com",
                now,
                listOf(
                        WorkflowReportWithParams("reportA", mapOf("param1" to "one", "param2" to "two")),
                        WorkflowReportWithParams("reportB", mapOf("param3" to "three"))
                ),
                mapOf("instanceA" to "pre-staging"),
                "branch1",
                "commit1"
        )

        val mockContext: ActionContext = mock {
            on { queryParams("key") } doReturn "adventurous_aardvark"
        }

        val mockRepo = mock<WorkflowRunRepository> {
            on { getWorkflowDetails("adventurous_aardvark") } doReturn workflowRun
        }

        val sut = WorkflowRunController(mockContext, mockRepo)

        val results = sut.getRunWorkflowDetails()
        assertThat(results).isEqualTo(workflowRun)
    }

    @Test
    fun `can throw UnknownObjectError if key is invalid`()
    {
        val mockContext: ActionContext = mock {
            on { queryParams("key") } doReturn "fakeKey"
        }

        val mockRepo = mock<WorkflowRunRepository> {
            on { getWorkflowDetails("fakeKey") } doThrow UnknownObjectError("key", "getWorkflowDetails")
        }

        val sut = WorkflowRunController(mockContext, mockRepo)

        assertThatThrownBy { sut.getRunWorkflowDetails() }
                .isInstanceOf(UnknownObjectError::class.java)
                .hasMessageContaining("Unknown get-workflow-details : 'key'")
    }

    @Test
    fun `can get getRunWorkflow breadcrumbs`()
    {
        val sut = WorkflowRunController(mock())
        val model = sut.getRunWorkflow()

        assertThat(model.breadcrumbs).containsExactly(IndexViewModel.breadcrumb,
                Breadcrumb("Run a workflow", "http://localhost:8888/run-workflow"))
    }
}