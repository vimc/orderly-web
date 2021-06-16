package org.vaccineimpact.orderlyweb.tests.integration_tests.tests.web

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.eclipse.jetty.http.HttpStatus
import org.jsoup.Jsoup
import org.junit.Test
import org.vaccineimpact.orderlyweb.ContentTypes
import org.vaccineimpact.orderlyweb.OrderlyServer
import org.vaccineimpact.orderlyweb.Serializer
import org.vaccineimpact.orderlyweb.controllers.web.WorkflowRunController
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.repositories.OrderlyWebWorkflowRunRepository
import org.vaccineimpact.orderlyweb.models.*
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.tests.insertUser
import org.vaccineimpact.orderlyweb.tests.integration_tests.tests.IntegrationTest
import spark.route.HttpMethod
import java.time.Instant

class WorkflowRunTests : IntegrationTest()
{
    private val runReportsPerm = setOf(ReifiedPermission("reports.run", Scope.Global()))

    @Test
    fun `only report runners can view workflow details`()
    {
        addWorkflowRunExample()

        val url = "/workflows/adventurous_aardvark/"
        val requiredPermissions = setOf(ReifiedPermission("reports.run", Scope.Global()))
        assertWebUrlSecured(url, requiredPermissions, contentType = ContentTypes.json)
    }

    @Test
    fun `can get workflow details`()
    {
        addWorkflowRunExample()

        val url = "/workflows/adventurous_aardvark/"
        val response = webRequestHelper.loginWithMontaguAndMakeRequest(
            url,
            setOf(ReifiedPermission("reports.run", Scope.Global())),
            method = HttpMethod.get,
            contentType = ContentTypes.json
        )

        assertSuccessful(response)
        assertJsonContentType(response)

        val responseData = JSONValidator.getData(response.text)
        assertThat(responseData["name"].textValue()).isEqualTo("Interim report")
        assertThat(responseData["key"].textValue()).isEqualTo("adventurous_aardvark")
        assertThat(responseData["email"].textValue()).isEqualTo("user@email.com")
    }

    @Test
    fun `does not get workflow details if key is invalid`()
    {
        val url = "/workflows/fakeKey/"
        val response = webRequestHelper.loginWithMontaguAndMakeRequest(
            url,
            setOf(ReifiedPermission("reports.run", Scope.Global())),
            method = HttpMethod.get,
            contentType = ContentTypes.json
        )

        assertJsonContentType(response)
        assertThat(response.statusCode).isNotEqualTo(HttpStatus.OK_200)
    }

    @Test
    fun `only report runners can see run workflow page`()
    {
        val url = "/run-workflow"
        assertWebUrlSecured(url, runReportsPerm)
    }

    @Test
    fun `correct workflow page is served`()
    {
        val sessionCookie = webRequestHelper.webLoginWithMontagu(runReportsPerm)
        val response = webRequestHelper.requestWithSessionCookie("/run-workflow", sessionCookie)
        assertThat(response.statusCode).isEqualTo(200)

        val page = Jsoup.parse(response.text)
        assertThat(page.selectFirst("#runWorkflowTabsVueApp")).isNotNull()
    }

    @Test
    fun `lists workflows`()
    {
        val sessionCookie = webRequestHelper.webLoginWithMontagu(runReportsPerm)

        val name = "Interim report"
        val key = "adventurous_aardvark"
        val email = "test.user@example.com"
        val date = Instant.now()

        val repo = OrderlyWebWorkflowRunRepository()
        repo.addWorkflowRun(WorkflowRun(name, key, email, date, emptyList(), emptyMap()))

        val response = webRequestHelper.requestWithSessionCookie(
            "/workflows?email=$email&namePrefix=${name.split(" ").first().toLowerCase()}",
            sessionCookie,
            ContentTypes.json
        )
        assertSuccessful(response)
        assertJsonContentType(response)

        val workflowRuns = JSONValidator.getData(response.text)
        assertThat(workflowRuns.size()).isEqualTo(1)
        val workflowRun = workflowRuns[0]
        assertThat(workflowRun["name"].textValue()).isEqualTo(name)
        assertThat(workflowRun["key"].textValue()).isEqualTo(key)
        assertThat(workflowRun["email"].textValue()).isEqualTo(email)
        assertThat(Instant.parse(workflowRun["date"].textValue())).isEqualTo(date)
    }

    @Test
    fun `runs workflow`()
    {
        val branch = "other"
        val commits = OrderlyServer(AppConfig()).get(
            "/git/commits",
            context = mock {
                on { queryString() } doReturn "branch=$branch"
            }
        )
        val commit = commits.listData(GitCommit::class.java).first().id

        val json = """
                {
                  "name": "full workflow",
                  "reports": [
                    {
                      "name": "other",
                      "params": {
                        "nmin": "0.25"
                      }
                    },
                    {
                      "name": "other",
                      "params": {
                        "nmin": "0.75"
                      }
                    },
                    {
                      "name": "minimal"
                    },
                    {
                      "name": "global"
                    }
                  ],
                  "changelog": {
                    "message": "message1",
                    "type": "internal"
                  },
                  "git_branch": "$branch",
                  "git_commit": "$commit"
                }
            """.trimIndent()

        val sessionCookie = webRequestHelper.webLoginWithMontagu(runReportsPerm)
        val response = webRequestHelper.requestWithSessionCookie(
            "/workflow",
            sessionCookie,
            ContentTypes.json,
            HttpMethod.post,
            json
        )
        assertSuccessful(response)
        assertJsonContentType(response)
        JSONValidator.validateAgainstOrderlySchema(response.text, "WorkflowRunResponse")

        val workflowRunResponse = Serializer.instance.gson.fromJson(
            JSONValidator.getData(response.text).toString(),
            WorkflowRunController.WorkflowRunResponse::class.java
        )

        val workflowRunRequest = Serializer.instance.gson.fromJson(json, WorkflowRunRequest::class.java)

        assertThat(workflowRunResponse.reports.size).isEqualTo(workflowRunRequest.reports.size)

        assertThat(workflowStatus(workflowRunResponse.key)).isEqualTo("success")
    }

    @Test
    fun `gets workflow status`()
    {
        val sessionCookie = webRequestHelper.webLoginWithMontagu(runReportsPerm)

        val runResponse = OrderlyServer(AppConfig()).post(
            "/v1/workflow/run/",
            """{"name": "minimal", "reports": [{"name": "minimal"}]}""",
            emptyMap()
        )
        val workflowRunResponse = runResponse.data(WorkflowRunController.WorkflowRunResponse::class.java)
        val repo = OrderlyWebWorkflowRunRepository()
        repo.addWorkflowRun(
            WorkflowRun(
                "minimal workflow",
                workflowRunResponse.key,
                "test.user@example.com",
                Instant.now(),
                listOf(WorkflowReportWithParams("minimal", emptyMap())),
                emptyMap()
            )
        )
        assertThat(workflowStatus(workflowRunResponse.key)).isEqualTo("success")

        val response = webRequestHelper.requestWithSessionCookie(
            "/workflows/${workflowRunResponse.key}/status",
            sessionCookie,
            ContentTypes.json
        )
        assertSuccessful(response)
        assertJsonContentType(response)
        val workflowRunStatus = Serializer.instance.gson.fromJson(
            JSONValidator.getData(response.text).toString(),
            WorkflowRunStatus::class.java
        )

        val orderlyServerResponse =
            OrderlyServer(AppConfig()).get("/v1/workflow/${workflowRunResponse.key}/status/", emptyMap())
        val workflowRunStatusResponse =
            orderlyServerResponse.data(WorkflowRunController.WorkflowRunStatusResponse::class.java)
        assertThat(workflowRunStatus.status).isEqualTo(workflowRunStatusResponse.status)
        assertThat(workflowRunStatus.reports[0].name).isEqualTo("minimal")
        assertThat(workflowRunStatus.reports[0].status).isEqualTo(workflowRunStatusResponse.reports[0].status)
        assertThat(workflowRunStatus.reports[0].key).isEqualTo(workflowRunStatusResponse.reports[0].key)
        assertThat(workflowRunStatus.reports[0].version).isEqualTo(workflowRunStatusResponse.reports[0].version)
    }

    @Test
    fun `returns error getting status for unknown workflow`()
    {
        val sessionCookie = webRequestHelper.webLoginWithMontagu(runReportsPerm)

        val response = webRequestHelper.requestWithSessionCookie(
            "/workflows/fake/status",
            sessionCookie,
            ContentTypes.json
        )
        assertThat(response.statusCode).isEqualTo(404)
    }

    private fun addWorkflowRunExample()
    {
        insertUser("user@email.com", "user.name")

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

        val sut = OrderlyWebWorkflowRunRepository()
        sut.addWorkflowRun(workflowRun)
    }

    private fun workflowStatus(key: String): String
    {
        for (i in 0..9)
        {
            val response = OrderlyServer(AppConfig()).get("/v1/workflow/$key/status/", emptyMap())
            val status = JSONValidator.getData(response.text)["status"].textValue()
            if (status in listOf("success", "error", "cancelled"))
            {
                return status
            }
            Thread.sleep(1000)
        }
        throw Exception("Workflow timeout")
    }
}
