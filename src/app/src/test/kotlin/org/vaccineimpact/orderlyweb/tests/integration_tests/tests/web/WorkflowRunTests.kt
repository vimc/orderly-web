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
    fun `runs basic workflow`()
    {
        assertThat(
            runWorkflow(
                """
                {
                  "name": "basic workflow",
                  "reports": [
                    {
                      "name": "minimal"
                    },
                    {
                      "name": "global"
                    }
                  ]
                }
            """.trimIndent()
            )
        ).isTrue()
    }

    @Test
    fun `runs full workflow`()
    {
        val branch = "other"
        val commits = OrderlyServer(AppConfig()).get(
            "/git/commits",
            context = mock {
                on { queryString() } doReturn "branch=$branch"
            }
        )
        val commit = commits.listData(GitCommit::class.java).first().id

        assertThat(
            runWorkflow(
                """
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
            )
        ).isTrue()
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

    private fun runWorkflow(json: String): Boolean
    {
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
        // TODO when mrc-2282 has been merged
//        JSONValidator.validateAgainstOrderlySchema(response.text, "WorkflowRunResponse")

        val workflowRunResponse = Serializer.instance.gson.fromJson(
            JSONValidator.getData(response.text).toString(),
            WorkflowRunController.WorkflowRunResponse::class.java
        )

        val workflowRunRequest = Serializer.instance.gson.fromJson(json, WorkflowRunRequest::class.java)

        assertThat(workflowRunResponse.reports.size).isEqualTo(workflowRunRequest.reports.size)

        var successful = false
        for (i in 0..9)
        {
            successful = workflowRunResponse.reports.all { key ->
                val status = OrderlyServer(AppConfig()).get(
                    "/v1/reports/$key/status/",
                    emptyMap()
                )
                JSONValidator.getData(status.text)["status"].textValue() == "success"
            }
            if (successful)
            {
                break
            }
            Thread.sleep(1000)
        }
        return successful
    }
}
