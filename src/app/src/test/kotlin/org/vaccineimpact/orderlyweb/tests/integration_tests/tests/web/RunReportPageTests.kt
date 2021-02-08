package org.vaccineimpact.orderlyweb.tests.integration_tests.tests.web

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.*
import org.eclipse.jetty.http.HttpStatus
import org.jsoup.Jsoup
import org.junit.Test
import org.vaccineimpact.orderlyweb.ContentTypes
import org.vaccineimpact.orderlyweb.OrderlyServer
import org.vaccineimpact.orderlyweb.controllers.web.ReportController
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.repositories.OrderlyReportRepository
import org.vaccineimpact.orderlyweb.db.repositories.OrderlyWebTagRepository
import org.vaccineimpact.orderlyweb.models.GitCommit
import org.vaccineimpact.orderlyweb.models.ReportWithDate
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
    fun `can return parameter data`()
    {
        val branch = "master"
        val commits = OrderlyServer(AppConfig()).get(
                "/git/commits",
                mock {
                    on { queryString() } doReturn "branch=$branch"
                }
        )
        val commit = commits.listData(GitCommit::class.java).first().id
        val url = "/report/minimal/parameters/?commit=$commit"

        val response = webRequestHelper.loginWithMontaguAndMakeRequest(url,
                setOf(ReifiedPermission("reports.run", Scope.Global())),
                contentType = ContentTypes.json)

        assertThat(response.statusCode).isEqualTo(HttpStatus.OK_200)
        assertJsonContentType(response)
        JSONValidator.validateAgainstOrderlySchema(response.text, "ReportParameters")
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

    @Test
    fun `lists runnable reports`()
    {
        val branch = "master"
        val commits = OrderlyServer(AppConfig()).get(
                "/git/commits",
                mock {
                    on { queryString() } doReturn "branch=$branch"
                }
        )
        val commit = commits.listData(GitCommit::class.java).first().id
        val repo = OrderlyReportRepository(true, false)
        val controller = ReportController(
                mock {
                    on { queryString() } doReturn "branch=$branch&commit=$commit"
                },
                mock(),
                OrderlyServer(AppConfig()),
                repo,
                mock()
        )
        val result = controller.getRunnableReports()
        assertThat(result).containsExactly(
                ReportWithDate("global", repo.getLatestVersion("global").date),
                ReportWithDate("minimal", repo.getLatestVersion("minimal").date)
        )
    }
}
