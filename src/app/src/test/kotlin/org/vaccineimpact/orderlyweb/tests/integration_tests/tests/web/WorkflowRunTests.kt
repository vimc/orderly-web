package org.vaccineimpact.orderlyweb.tests.integration_tests.tests.web

import org.assertj.core.api.Assertions
import org.jsoup.Jsoup
import org.junit.Test
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.tests.integration_tests.tests.IntegrationTest

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
}