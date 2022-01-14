package org.vaccineimpact.orderlyweb.tests.integration_tests.tests.web

import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.TextNode
import com.github.fge.jackson.JsonLoader
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
        val runWorkflowReport = listOf(
            WorkflowRunReport(
                "adventurous_aardvark",
                "preterrestrial_andeancockoftherock",
                "Report A",
                emptyMap()
            )
        )

        val repo = OrderlyWebWorkflowRunRepository()
        repo.addWorkflowRun(WorkflowRun(name, key, email, date, runWorkflowReport, emptyMap()))

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
        val commit = getGitBranchCommit("other")

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
                      "name": "minimal",
                      "params": {}
                    },
                    {
                      "name": "global",
                      "params": {}
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
                listOf(
                    WorkflowRunReport(
                        workflowRunResponse.key,
                        workflowRunResponse.reports.first(),
                        "minimal",
                        emptyMap()
                    )
                ),
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

    @Test
    fun `validates workflow with missing branch and commit parameters`()
    {
        validateWorkflowWithDefaultBranchAncCommit("/workflow/validate")
    }

    @Test
    fun `validates workflow with empty branch and commit parameters`()
    {
        validateWorkflowWithDefaultBranchAncCommit("/workflow/validate?branch&commit")
    }

    private fun validateWorkflowWithDefaultBranchAncCommit(url: String)
    {
        val sessionCookie = webRequestHelper.webLoginWithMontagu(runReportsPerm)
        val formData = """
        --XXXX
        Content-Disposition: form-data; name="file"; filename="test.csv"
        Content-Type: text/csv
        
        report
        global
        minimal
        --XXXX--
        """.trimIndent()
        val response = webRequestHelper.requestWithSessionCookie(
            url,
            sessionCookie,
            ContentTypes.json,
            HttpMethod.post,
            formData,
            mapOf("Content-Type" to ContentTypes.multipart + ";boundary=XXXX")
        )
        assertSuccessful(response)
        assertJsonContentType(response)

        val reports = JSONValidator.getData(response.text) as ArrayNode
        assertThat(reports.count()).isEqualTo(2)
        val report1 = reports[0]
        assertThat(report1["name"].asText()).isEqualTo("global")
        assertThat(report1["params"].isEmpty(null)).isTrue()


        val report2 = reports[1]
        assertThat(report2["name"].asText()).isEqualTo("minimal")
        assertThat(report2["params"].isEmpty(null)).isTrue()
    }

    @Test
    fun `validates workflow for non-default git branch and commit`()
    {
        val branch = "other"
        val commit = getGitBranchCommit(branch)

        val sessionCookie = webRequestHelper.webLoginWithMontagu(runReportsPerm)
        val formData = """
        --XXXX
        Content-Disposition: form-data; name="file"; filename="test.csv"
        Content-Type: text/csv
        
        report,nmin
        other,1
        other,2
        --XXXX--
        """.trimIndent()
        val response = webRequestHelper.requestWithSessionCookie(
                "/workflow/validate?branch=$branch&commit=$commit",
                sessionCookie,
                ContentTypes.json,
                HttpMethod.post,
                formData,
                mapOf("Content-Type" to ContentTypes.multipart + ";boundary=XXXX")
        )
        assertSuccessful(response)
        assertJsonContentType(response)

        val reports = JSONValidator.getData(response.text) as ArrayNode
        assertThat(reports.count()).isEqualTo(2)
        val report1 = reports[0]
        assertThat(report1["name"].asText()).isEqualTo("other")
        assertThat(report1["params"]["nmin"].asText()).isEqualTo("1")

        val report2 = reports[1]
        assertThat(report2["name"].asText()).isEqualTo("other")
        assertThat(report2["params"]["nmin"].asText()).isEqualTo("2")
    }

    @Test
    fun `returns workflow header validation error`()
    {
       val sessionCookie = webRequestHelper.webLoginWithMontagu(runReportsPerm)
        val formData = """
        --XXXX
        Content-Disposition: form-data; name="file"; filename="test.csv"
        Content-Type: text/csv
        
        BADHEADER,disease,year
        test1,HepB,2020
        --XXXX--
        """.trimIndent()
        val response = webRequestHelper.requestWithSessionCookie(
                "/workflow/validate",
                sessionCookie,
                ContentTypes.json,
                HttpMethod.post,
                formData,
                mapOf("Content-Type" to ContentTypes.multipart + ";boundary=XXXX")
        )
        assertThat(response.statusCode).isEqualTo(400)
        JSONValidator.validateError(response.text, "bad-request", "First header must be 'report'")
    }

    @Test
    fun `returns workflow report validation errors`()
    {
        val branch = "other"
        val commit = getGitBranchCommit(branch)

        val sessionCookie = webRequestHelper.webLoginWithMontagu(runReportsPerm)
        val formData = """
        --XXXX
        Content-Disposition: form-data; name="file"; filename="test.csv"
        Content-Type: text/csv
        
        report,nmin
        other,1
        other,
        other,3,4
        nonexistent,5
        --XXXX--
        """.trimIndent()
        val response = webRequestHelper.requestWithSessionCookie(
                "/workflow/validate?branch=$branch&commit=$commit",
                sessionCookie,
                ContentTypes.json,
                HttpMethod.post,
                formData,
                mapOf("Content-Type" to ContentTypes.multipart + ";boundary=XXXX")
        )

        assertThat(response.statusCode).isEqualTo(400)
        val errors = JsonLoader.fromString(response.text)["errors"] as ArrayNode
        assertThat(errors.count()).isEqualTo(3)
        assertThat(errors[0]["message"].asText()).isEqualTo("Row 4: row should contain 2 values, 3 values found")
        assertThat(errors[0]["code"].asText()).isEqualTo("bad-request")
        assertThat(errors[1]["message"].asText()).isEqualTo("Row 3, column 2: required parameter 'nmin' was not provided for report 'other'")
        assertThat(errors[1]["code"].asText()).isEqualTo("bad-request")
        assertThat(errors[2]["message"].asText()).isEqualTo("Row 5, column 1: report 'nonexistent' not found in Orderly")
        assertThat(errors[2]["code"].asText()).isEqualTo("bad-request")
    }

    @Test
    fun `returns expected error when empty file data passed`()
    {
        val sessionCookie = webRequestHelper.webLoginWithMontagu(runReportsPerm)
        val formData = """
        --XXXX
        Content-Disposition: form-data; name="file"; filename="test.csv"
        Content-Type: text/csv

        --XXXX--
        """.trimIndent()
        val response = webRequestHelper.requestWithSessionCookie(
                "/workflow/validate",
                sessionCookie,
                ContentTypes.json,
                HttpMethod.post,
                formData,
                mapOf("Content-Type" to ContentTypes.multipart + ";boundary=XXXX")
        )
        assertThat(response.statusCode).isEqualTo(400)
        val errors = JsonLoader.fromString(response.text)["errors"] as ArrayNode
        assertThat(errors.count()).isEqualTo(1)
        assertThat(errors[0]["message"].asText()).isEqualTo("File contains no rows")
        assertThat(errors[0]["code"].asText()).isEqualTo("bad-request")
    }

    @Test
    fun `returns expected error when empty form data passed`()
    {
        val sessionCookie = webRequestHelper.webLoginWithMontagu(runReportsPerm)
        val response = webRequestHelper.requestWithSessionCookie(
                "/workflow/validate",
                sessionCookie,
                ContentTypes.json,
                HttpMethod.post,
                "",
                mapOf("Content-Type" to ContentTypes.multipart + ";boundary=XXXX")
        )
        assertThat(response.statusCode).isEqualTo(400)
        val errors = JsonLoader.fromString(response.text)["errors"] as ArrayNode
        assertThat(errors.count()).isEqualTo(1)
        assertThat(errors[0]["message"].asText()).isEqualTo("No data provided")
        assertThat(errors[0]["code"].asText()).isEqualTo("bad-request")
    }

    @Test
    fun `can get workflow summary`()
    {
        val ref = getGitBranchCommit("master")
        val sessionCookie = webRequestHelper.webLoginWithMontagu(runReportsPerm)
        val response = webRequestHelper.requestWithSessionCookie(
            "/workflows/summary",
            sessionCookie,
            ContentTypes.json,
            HttpMethod.post,
            """{
                "ref": "$ref", 
                "reports": [
                {"name": "global", "params": {"nmin", "20"}},
                {"name": "global", "params": {"nmin", "10"}}
                ]}""".trimIndent()
        )

        assertSuccessful(response)
        assertJsonContentType(response)
        val json = JsonLoader.fromString(response.text)
        val data = json["data"]
        val reports = data["reports"] as ArrayNode
        assertThat(reports.count()).isEqualTo(1)
        assertThat((reports[0]["name"] as TextNode).textValue()).isEqualTo("global")
        assertThat(reports[0]["params"].count()).isEqualTo(2)
        assertThat(reports[0]["params"][0]["nmin"].asText()).isEqualTo("20")
        assertThat(reports[0]["params"][1]["nmin"].asText()).isEqualTo("10")
        assertThat((data["missing_dependencies"]["global"] as ArrayNode).count()).isEqualTo(0)

        JSONValidator.validateAgainstOrderlySchema(response.text, "WorkflowSummaryResponse")
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
                WorkflowRunReport(
                    "adventurous_aardvark",
                    "adventurous_key",
                    "report one",
                    mapOf("param1" to "one", "param1" to "one", "param2" to "two")
                ),
                WorkflowRunReport(
                    "adventurous_aardvark",
                    "adventurous_key2",
                    "report two",
                    mapOf("param1" to "one", "param2" to "three")
                )
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
