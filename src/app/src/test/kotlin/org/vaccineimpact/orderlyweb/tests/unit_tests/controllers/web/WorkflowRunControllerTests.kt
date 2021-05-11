package org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.web

import com.github.fge.jackson.JsonLoader
import com.github.fge.jsonschema.main.JsonSchemaFactory
import com.nhaarman.mockito_kotlin.*
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.pac4j.core.profile.CommonProfile
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.OrderlyServerAPI
import org.vaccineimpact.orderlyweb.OrderlyServerResponse
import org.vaccineimpact.orderlyweb.Serializer
import org.vaccineimpact.orderlyweb.controllers.web.WorkflowRunController
import org.vaccineimpact.orderlyweb.db.repositories.WorkflowRunRepository
import org.vaccineimpact.orderlyweb.errors.BadRequest
import org.vaccineimpact.orderlyweb.errors.UnknownObjectError
import org.vaccineimpact.orderlyweb.models.*
import org.vaccineimpact.orderlyweb.viewmodels.Breadcrumb
import org.vaccineimpact.orderlyweb.viewmodels.IndexViewModel
import java.io.File
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

        val mockContext = mock<ActionContext> {
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
    fun `throws UnknownObjectError if key is invalid`()
    {
        val mockContext = mock<ActionContext> {
            on { params(":key") } doReturn "fakeKey"
        }

        val mockRepo = mock<WorkflowRunRepository> {
            on { getWorkflowRunDetails("fakeKey") } doThrow UnknownObjectError("key", "workflow")
        }

        val sut = WorkflowRunController(mockContext, mockRepo, mock())

        assertThatThrownBy { sut.getWorkflowRunDetails() }
            .isInstanceOf(UnknownObjectError::class.java)
            .hasMessageContaining("Unknown workflow : 'key'")
    }

    @Test
    fun `can deserialize a workflow description`()
    {
        val json = """
            {
              "name": "workflow1",
              "instances": {
                "source": "instance1"
              },
              "changelog": {
                "message": "message1",
                "type": "type1"
              },
              "git_branch": "branch1",
              "git_commit": "commit1",
              "reports": [
                {
                  "name": "report1",
                  "params": {
                    "param1": "value1"
                  }
                },
                {
                  "name": "report2",
                  "params": {
                    "param2": "value2"
                  }
                }
              ]
            }
        """.trimIndent()

        assertThat(validateAgainstSchema(json)).isTrue()

        var workflowRunRequest = Serializer.instance.gson.fromJson(json, WorkflowRunRequest::class.java)
        assertThat(workflowRunRequest).isEqualTo(getWorkflowRunRequestExample())
    }

    @Test
    fun `can run a workflow`()
    {
        val workflowRunRequest = getWorkflowRunRequestExample()

        val context = mock<ActionContext> {
            on { getRequestBody() } doReturn Serializer.instance.gson.toJson(workflowRunRequest)
            on { userProfile } doReturn CommonProfile().apply { id = "test@user.com" }
        }

        val mockAPIResponseText = """{"data": {"workflow_key": "workflow_key1", "reports": ["report_key1"]}}"""

        val mockAPIResponse = OrderlyServerResponse(mockAPIResponseText, 200)

        val apiClient = mock<OrderlyServerAPI> {
            on { post(any(), any<String>(), any()) } doReturn mockAPIResponse
        }

        val repo = mock<WorkflowRunRepository>()
        val sut = WorkflowRunController(context, repo, apiClient)
        val result = sut.createWorkflowRun()

        verify(apiClient).post(
            "/v1/workflow/run/",
            Serializer.instance.gson.toJson(
                mapOf(
                    "changelog" to mapOf(
                        "message" to workflowRunRequest.changelog!!.message,
                        "type" to workflowRunRequest.changelog!!.type
                    ),
                    "ref" to workflowRunRequest.gitCommit,
                    "reports" to listOf(
                        mapOf(
                            "name" to workflowRunRequest.reports[0].name,
                            "params" to workflowRunRequest.reports[0].params,
                            "instance" to workflowRunRequest.instances!!.values.first()
                        ),
                        mapOf(
                            "name" to workflowRunRequest.reports[1].name,
                            "params" to workflowRunRequest.reports[1].params,
                            "instance" to workflowRunRequest.instances!!.values.first()
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
            assertThat(it.name).isEqualTo(workflowRunRequest.name)
            assertThat(it.key).isEqualTo("workflow_key1")
            assertThat(it.email).isEqualTo("test@user.com")
            assertThat(it.reports).isEqualTo(workflowRunRequest.reports)
            assertThat(it.instances).isEqualTo(workflowRunRequest.instances)
            assertThat(it.gitBranch).isEqualTo(workflowRunRequest.gitBranch)
            assertThat(it.gitCommit).isEqualTo(workflowRunRequest.gitCommit)
        })
    }

    @Test
    fun `rejects an invalid workflow`()
    {
        val json = """
            {
              "name": "workflow1",
              "reports": "report1"
            }
        """.trimIndent()

        assertThat(validateAgainstSchema(json)).isFalse()

        val context = mock<ActionContext> {
            on { getRequestBody() } doReturn json
            on { userProfile } doReturn CommonProfile().apply { id = "test@user.com" }
        }

        val sut = WorkflowRunController(context, mock(), mock())
        assertThatThrownBy { sut.createWorkflowRun() }
            .isInstanceOf(BadRequest::class.java)
            .hasMessageContaining("Invalid workflow description")
    }

    @Test
    fun `server workflow error is passed through`()
    {
        val json = """
            {
              "name": "workflow1",
              "reports": [{"name": "report1"}]
            }
        """.trimIndent()

        assertThat(validateAgainstSchema(json)).isTrue()

        val context = mock<ActionContext> {
            on { getRequestBody() } doReturn """{"name": "workflow1", "reports": [{"name": "report1"}]}"""
            on { userProfile } doReturn CommonProfile().apply { id = "test@user.com" }
        }

        val mockResponse = """{"status": "failure", "data": null, "errors": []}"""

        val apiClient = mock<OrderlyServerAPI> {
            on { post(any(), any<String>(), any()) } doReturn OrderlyServerResponse(
                mockResponse,
                400
            )
        }

        val sut = WorkflowRunController(context, mock(), apiClient)
        val response = sut.createWorkflowRun()
        verify(context).setStatusCode(400)
        assertThat(response).isEqualTo(mockResponse)
    }

    @Test
    fun `empty changelog and ref are omitted from orderly server workflow run request`()
    {
        val json = """
            {
              "name": "workflow1",
              "reports": [{"name": "report1"}]
            }
        """.trimIndent()

        assertThat(validateAgainstSchema(json)).isTrue()

        val context = mock<ActionContext> {
            on { getRequestBody() } doReturn json
            on { userProfile } doReturn CommonProfile().apply { id = "test@user.com" }
        }

        val apiClient = mock<OrderlyServerAPI> {
            on { post(any(), any<String>(), any()) } doReturn OrderlyServerResponse(
                """{"data": {"workflow_key": "workflow_key1", "reports": ["report_key1"]}}""",
                200
            )
        }

        val sut = WorkflowRunController(context, mock(), apiClient)
        sut.createWorkflowRun()

        verify(apiClient).post(
            "/v1/workflow/run/",
            Serializer.instance.gson.toJson(
                mapOf(
                    "reports" to listOf(
                        mapOf(
                            "name" to "report1",
                            "params" to null
                        )
                    )
                )
            ),
            emptyMap()
        )
    }

    @Test
    fun `can get the status of a workflow`()
    {
        val context = mock<ActionContext> {
            on { params(":key") } doReturn "workflow_key1"
            on { userProfile } doReturn CommonProfile().apply { id = "test@user.com" }
        }

        val mockAPIResponseText = """{"data": {"workflow_key": "workflow_key1", "status": "success"}}"""

        val mockAPIResponse = OrderlyServerResponse(mockAPIResponseText, 200)

        val apiClient = mock<OrderlyServerAPI> {
            on { get(any(), any<Map<String, String>>()) } doReturn mockAPIResponse
        }

        val repo = mock<WorkflowRunRepository>()
        val sut = WorkflowRunController(context, repo, apiClient)
        val result = sut.getWorkflowRunStatus()

        verify(apiClient).get("/v1/workflow/workflow_key1/status/", emptyMap())

        assertThat(
            Serializer.instance.gson.fromJson(
                JsonLoader.fromString(result)["data"].toString(),
                WorkflowRunController.WorkflowRunStatusResponse::class.java
            )
        ).isEqualTo(
            WorkflowRunController.WorkflowRunStatusResponse("workflow_key1", "success")
        )

        verify(repo).updateWorkflowRun("workflow_key1", "success")
    }

    @Test
    fun `passes through error when getting status of a workflow`()
    {
        val context = mock<ActionContext> {
            on { params(":key") } doReturn "workflow_key1"
            on { userProfile } doReturn CommonProfile().apply { id = "test@user.com" }
        }

        val mockResponse = """{"status": "failure", "data": null, "errors": []}"""

        val apiClient = mock<OrderlyServerAPI> {
            on { get(any(), any<Map<String, String>>()) } doReturn OrderlyServerResponse(
                mockResponse,
                400
            )
        }

        val sut = WorkflowRunController(context, mock(), apiClient)
        val response = sut.getWorkflowRunStatus()
        verify(context).setStatusCode(400)
        assertThat(response).isEqualTo(mockResponse)
    }

    private fun getWorkflowRunRequestExample() =
        WorkflowRunRequest(
            "workflow1",
            listOf(
                WorkflowReportWithParams("report1", mapOf("param1" to "value1")),
                WorkflowReportWithParams("report2", mapOf("param2" to "value2"))
            ),
            mapOf("source" to "instance1"),
            WorkflowChangelog("message1", "type1"),
            "branch1",
            "commit1"
        )

    private fun validateAgainstSchema(json: String) =
        JsonSchemaFactory.byDefault()
            .getJsonSchema(File("../../docs/spec/RunWorkflow.schema.json").toURI().toString())
            .validate(JsonLoader.fromString(json))
            .isSuccess
}
