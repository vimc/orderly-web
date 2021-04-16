package org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.web

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.web.WorkflowRunController
import org.vaccineimpact.orderlyweb.db.repositories.WorkflowRunRepository
import org.vaccineimpact.orderlyweb.models.WorkflowRunSummary
import org.vaccineimpact.orderlyweb.viewmodels.Breadcrumb
import org.vaccineimpact.orderlyweb.viewmodels.IndexViewModel
import java.time.Instant

class WorkflowRunControllerTests
{
    @Test
    fun `can get getRunWorkflow breadcrumbs`()
    {

        val sut = WorkflowRunController(mock())
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
}
