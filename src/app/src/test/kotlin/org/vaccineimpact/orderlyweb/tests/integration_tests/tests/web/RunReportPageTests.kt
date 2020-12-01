package org.vaccineimpact.orderlyweb.tests.integration_tests.tests.web

import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.*
import org.jsoup.Jsoup
import org.junit.Test
import org.vaccineimpact.orderlyweb.OrderlyServer
import org.vaccineimpact.orderlyweb.controllers.web.ReportController
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.repositories.OrderlyWebTagRepository
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.tests.integration_tests.tests.IntegrationTest

class RunReportPageTests : IntegrationTest()
{
    private val runReportsPerm = setOf(ReifiedPermission("reports.run", Scope.Global()))

    @Test
    fun `only report runners can see the page`()
    {
        val url = "/run-report"
        assertWebUrlSecured(url, runReportsPerm)
    }

    @Test
    fun `correct page is served`()
    {
        val sessionCookie = webRequestHelper.webLoginWithMontagu(runReportsPerm)
        val response = webRequestHelper.requestWithSessionCookie("/run-report", sessionCookie)
        assertThat(response.statusCode).isEqualTo(200)

        val page = Jsoup.parse(response.text)
        assertThat(page.selectFirst("#runReportVueApp")).isNotNull()
    }

    @Test
    fun `fetches git branches`()
    {
        val controller = ReportController(mock(),
                mock(),
                OrderlyServer(AppConfig()),
                mock(),
                OrderlyWebTagRepository())

        val result = controller.getRunReport()
        assertThat(result.gitBranches).containsExactly("master", "other")
    }
}
