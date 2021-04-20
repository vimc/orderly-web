package org.vaccineimpact.orderlyweb.tests.integration_tests.tests.web

import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.jsoup.Jsoup
import org.junit.Test
import org.vaccineimpact.orderlyweb.ContentTypes
import org.vaccineimpact.orderlyweb.db.repositories.OrderlyWebWorkflowRunRepository
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.WorkflowRun
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.tests.integration_tests.tests.IntegrationTest
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

        val workflowRun = JSONValidator.getData(response.text)[0]
        assertThat(workflowRun["name"].textValue()).isEqualTo(name)
        assertThat(workflowRun["key"].textValue()).isEqualTo(key)
        assertThat(workflowRun["email"].textValue()).isEqualTo(email)
        assertThat(Instant.parse(workflowRun["date"].textValue())).isEqualTo(date)
    }
}