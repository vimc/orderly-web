package org.vaccineimpact.orderlyweb.tests.integration_tests.tests.web

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
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
import org.vaccineimpact.orderlyweb.tests.integration_tests.tests.IntegrationTest
import spark.route.HttpMethod
import java.time.Instant

class WorkflowRunTests : IntegrationTest()
{
    private val runReportsPerm = setOf(ReifiedPermission("reports.run", Scope.Global()))

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
        Assertions.assertThat(response.statusCode).isEqualTo(200)

        val page = Jsoup.parse(response.text)
        Assertions.assertThat(page.selectFirst("#runWorkflowTabsVueApp")).isNotNull()
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

    private fun runWorkflow(workflowRunRequest: WorkflowRunRequest): Boolean
    {
        val sessionCookie = webRequestHelper.webLoginWithMontagu(runReportsPerm)

        val response = webRequestHelper.requestWithSessionCookie(
            "/workflow",
            sessionCookie,
            ContentTypes.json,
            HttpMethod.post,
            Serializer.instance.gson.toJson(workflowRunRequest)
        )
        assertSuccessful(response)
        assertJsonContentType(response)
        // TODO when mrc-2282 has been merged
//        JSONValidator.validateAgainstOrderlySchema(response.text, "WorkflowRunResponse")

        val workflowRunResponse = Serializer.instance.gson.fromJson(
            JSONValidator.getData(response.text).toString(),
            WorkflowRunController.WorkflowRunResponse::class.java
        )

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

    @Test
    fun `runs basic workflow`()
    {
        assertThat(
            runWorkflow(
                WorkflowRunRequest(
                    "basic workflow",
                    listOf(
                        WorkflowReportWithParams("minimal", emptyMap()),
                        WorkflowReportWithParams("global", emptyMap())
                    ),
                    emptyMap()
                )
            )
        ).isTrue()
    }

    @Test
    fun `runs full workflow`()
    {
        val sessionCookie = webRequestHelper.webLoginWithMontagu(runReportsPerm)

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
                WorkflowRunRequest(
                    "full workflow",
                    listOf(
                        WorkflowReportWithParams("other", mapOf("nmin" to "0.25")),
                        WorkflowReportWithParams("other", mapOf("nmin" to "0.75")),
                        WorkflowReportWithParams("minimal", emptyMap()),
                        WorkflowReportWithParams("global", emptyMap())
                    ),
                    emptyMap(),
                    WorkflowChangelog("message1", "internal"),
                    branch,
                    commit
                )
            )
        ).isTrue()
    }
}
