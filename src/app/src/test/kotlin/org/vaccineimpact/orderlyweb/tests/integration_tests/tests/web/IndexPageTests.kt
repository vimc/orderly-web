package org.vaccineimpact.orderlyweb.tests.integration_tests.tests.web

import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.jsoup.Jsoup
import org.junit.Test
import org.vaccineimpact.orderlyweb.ContentTypes
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.test_helpers.insertGlobalPinnedReport
import org.vaccineimpact.orderlyweb.tests.integration_tests.tests.IntegrationTest

class IndexPageTests : IntegrationTest()
{
    private val readReports = setOf(ReifiedPermission("reports.read", Scope.Global()))
    private val url = "/"

    @Test
    fun `all authenticated users can get index page`()
    {
        assertWebUrlSecured(url, setOf())
    }

    @Test
    fun `pinned report zip file can be downloaded`()
    {
        insertGlobalPinnedReport("other", 0)

        val sessionCookie = webRequestHelper.webLoginWithMontagu(readReports)
        val response = webRequestHelper.requestWithSessionCookie("/", sessionCookie)
        val page = Jsoup.parse(response.text)

        val href = page.selectFirst("#pinned-reports a.pinned-report-link").attr("href")

        val result = webRequestHelper.requestWithSessionCookie(href, sessionCookie, ContentTypes.binarydata)

        Assertions.assertThat(result.statusCode).isEqualTo(200)
    }

    @Test
    fun `pinned report version link works`()
    {
        insertGlobalPinnedReport("other", 0)

        val sessionCookie = webRequestHelper.webLoginWithMontagu(readReports)
        val response = webRequestHelper.requestWithSessionCookie("/", sessionCookie)
        val page = Jsoup.parse(response.text)

        val href = page.selectFirst("#pinned-reports div.card-header a").attr("href")

        val result = webRequestHelper.requestWithSessionCookie(href, sessionCookie, ContentTypes.html)

        Assertions.assertThat(result.statusCode).isEqualTo(200)
    }
}