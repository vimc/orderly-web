package org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.web

import com.github.fge.jackson.JsonLoader
import com.nhaarman.mockito_kotlin.*
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.pac4j.core.profile.CommonProfile
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.OrderlyServerAPI
import org.vaccineimpact.orderlyweb.OrderlyServerResponse
import org.vaccineimpact.orderlyweb.Serializer
import org.vaccineimpact.orderlyweb.controllers.web.WorkflowRunController
import org.vaccineimpact.orderlyweb.db.repositories.WorkflowRunRepository
import org.vaccineimpact.orderlyweb.errors.UnknownObjectError
import org.vaccineimpact.orderlyweb.models.*
import org.vaccineimpact.orderlyweb.viewmodels.Breadcrumb
import org.vaccineimpact.orderlyweb.viewmodels.IndexViewModel
import java.time.Instant

class WorkflowRunControllerTests
{
    @Test
    fun `can getRunWorkflow breadcrumbs`()
    {
        val sut = WorkflowRunController(mock(), mock(), mock())
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
            on { getWorkflowRunSummaries("user@email.com", "Interim") } doReturn workflowRunSummaries
        }
        val sut = WorkflowRunController(context, repo, mock())
        assertThat(sut.getWorkflowRunSummaries()).isEqualTo(workflowRunSummaries)
    }

    @Test
    fun `can get workflow details`()
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

        val sut = WorkflowRunController(mockContext, mockRepo, mock())

        val results = sut.getWorkflowRunDetails()
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

        val sut = WorkflowRunController(mockContext, mockRepo, mock())

        Assertions.assertThatThrownBy { sut.getWorkflowRunDetails() }
            .isInstanceOf(UnknownObjectError::class.java)
            .hasMessageContaining("Unknown workflow : 'key'")
    }

    @Test
    fun `can run a workflow`()
    {
        val workflowRunRequest = WorkflowRunRequest(
            "workflow1",
            listOf(WorkflowReportWithParams("report1", mapOf("param1" to "value1"))),
            mapOf("database1" to "instance1"),
            WorkflowChangelog("message1", "type1"),
            "branch1",
            "commit1"
        )

        val context = mock<ActionContext> {
            on { getRequestBody() } doReturn Serializer.instance.gson.toJson(workflowRunRequest)
            on { userProfile } doReturn CommonProfile().apply { id = "test@user.com" }
        }

        val mockAPIResponseText = """{"data": {"workflow_key": "workflow_key1", "reports": ["report_key1"]}}"""

        val mockAPIResponse = OrderlyServerResponse(mockAPIResponseText, 200)

        val apiClient: OrderlyServerAPI = mock {
            on { post(any(), any<String>(), any()) } doReturn mockAPIResponse
        }

        val repo = mock<WorkflowRunRepository>()
        val sut = WorkflowRunController(context, repo, apiClient)
        val result = sut.createWorkflowRun()

        verify(apiClient).post(
            "/v1/workflow/run/",
            Serializer.instance.gson.toJson(
                mapOf(
                    "changelog" to workflowRunRequest.changelog,
                    "ref" to workflowRunRequest.gitCommit,
                    "reports" to listOf(
                        mapOf(
                            "name" to workflowRunRequest.reports[0].name,
                            "params" to workflowRunRequest.reports[0].params,
                            "instance" to workflowRunRequest.instances
                        )
                    )
                )
            ),
            emptyMap()
        )

        assertThat(
            Serializer.instance.gson.fromJson(
                JsonLoader.fromString(result)["data"].toString(),
                WorkflowRunController.WorkflowRunResponse::class.java
            )
        ).isEqualTo(
            WorkflowRunController.WorkflowRunResponse("workflow_key1", listOf("report_key1"))
        )

        verify(repo).addWorkflowRun(check {
            assertThat(it.name).isEqualTo("workflow1")
            assertThat(it.key).isEqualTo("workflow_key1")
            assertThat(it.email).isEqualTo("test@user.com")
            assertThat(it.reports).isEqualTo(listOf(WorkflowReportWithParams("report1", mapOf("param1" to "value1"))))
            assertThat(it.instances).isEqualTo(mapOf("database1" to "instance1"))
            assertThat(it.gitBranch).isEqualTo("branch1")
            assertThat(it.gitCommit).isEqualTo("commit1")
        })
    }
}
