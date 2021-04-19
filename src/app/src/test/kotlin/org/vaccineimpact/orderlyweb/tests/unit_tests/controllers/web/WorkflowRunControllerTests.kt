package org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.web

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.doThrow
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.web.WorkflowRunController
import org.vaccineimpact.orderlyweb.db.repositories.WorkflowRunRepository
import org.vaccineimpact.orderlyweb.errors.UnknownObjectError
import org.vaccineimpact.orderlyweb.models.WorkflowReportWithParams
import org.vaccineimpact.orderlyweb.models.WorkflowRun
import org.vaccineimpact.orderlyweb.models.WorkflowRunSummary
import org.vaccineimpact.orderlyweb.viewmodels.Breadcrumb
import org.vaccineimpact.orderlyweb.viewmodels.IndexViewModel
import java.time.Instant

class WorkflowRunControllerTests
{
    @Test
    fun `can getRunWorkflow breadcrumbs`()
    {

        val sut = WorkflowRunController(mock(), mock())
        val model = sut.getRunWorkflow()

        assertThat(model.breadcrumbs).containsExactly(
            IndexViewModel.breadcrumb,
            Breadcrumb("Run a workflow", "http://localhost:8888/run-workflow")
        )
    }

    @Test
    fun `can get workflow summaries`()
    {
        val context = mock<ActionContext> {
            on { queryParams("email") } doReturn "user@email.com"
            on { queryParams("namePrefix") } doReturn "Interim"
        }
        val workflowRunSummaries = listOf(
            WorkflowRunSummary(
                "Interim report",
                "adventurous_aardvark",
                "user@email.com",
                Instant.now()
            )
        )
        val repo = mock<WorkflowRunRepository> {
            on { this.getWorkflowRunSummaries("user@email.com", "Interim") } doReturn workflowRunSummaries
        }
        val sut = WorkflowRunController(context, repo)
        assertThat(sut.getWorkflowRunSummaries()).isEqualTo(workflowRunSummaries)
    }

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
            on { params(":key") } doReturn "adventurous_aardvark"
        }

        val mockRepo = mock<WorkflowRunRepository> {
            on { getWorkflowRunDetails("adventurous_aardvark") } doReturn workflowRun
        }

        val sut = WorkflowRunController(mockContext, mockRepo)

        val results = sut.getRunWorkflowDetails()
        assertThat(results).isEqualTo(workflowRun)
    }

    @Test
    fun `can throw UnknownObjectError if key is invalid`()
    {
        val mockContext: ActionContext = mock {
            on { params(":key") } doReturn "fakeKey"
        }

        val mockRepo = mock<WorkflowRunRepository> {
            on { getWorkflowRunDetails("fakeKey") } doThrow UnknownObjectError("key", "workflow")
        }

        val sut = WorkflowRunController(mockContext, mockRepo)

        Assertions.assertThatThrownBy { sut.getRunWorkflowDetails() }
                .isInstanceOf(UnknownObjectError::class.java)
                .hasMessageContaining("Unknown workflow : 'key'")
    }
}
