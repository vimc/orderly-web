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
    private val readOther = setOf(ReifiedPermission("reports.read", Scope.Specific("report", "other")))

    @Test
    fun `all authenticated users can get index page`()
    {
        val sessionCookie = webRequestHelper.webLoginWithMontagu(setOf())
        val response = webRequestHelper.requestWithSessionCookie("/", sessionCookie)
        assertThat(response.statusCode).isEqualTo(200)

        val page = Jsoup.parse(response.text)
        assertThat(page.selectFirst("h1").text()).isEqualTo("Find a report")
    }

    @Test
    fun `can download pinned report zip file`()
    {
        insertGlobalPinnedReport("minimal", 0)
        insertGlobalPinnedReport("other", 1)

        val sessionCookie = webRequestHelper.webLoginWithMontagu(readOther)
        val response = webRequestHelper.requestWithSessionCookie("/", sessionCookie)
        val page = Jsoup.parse(response.text)

        val pinnedReportsDownloadButtons = page.select("#pinned-reports a.pinned-report-link")
        assertThat(pinnedReportsDownloadButtons.count()).isEqualTo(1) // user can only read "other"

        val href = pinnedReportsDownloadButtons.first().attr("href")
        val result = webRequestHelper.requestWithSessionCookie(href, sessionCookie, ContentTypes.binarydata)

        Assertions.assertThat(result.statusCode).isEqualTo(200)
    }

    @Test
    fun `pinned report version link works`()
    {
        insertGlobalPinnedReport("minimal", 0)
        insertGlobalPinnedReport("other", 1)

        val sessionCookie = webRequestHelper.webLoginWithMontagu(readOther)
        val response = webRequestHelper.requestWithSessionCookie("/", sessionCookie)
        val page = Jsoup.parse(response.text)

        val pinnedReportsLinks = page.select("#pinned-reports div.card-header a")
        assertThat(pinnedReportsLinks.count()).isEqualTo(1) // user can only read "other"
        val href = pinnedReportsLinks.first().attr("href")

        val result = webRequestHelper.requestWithSessionCookie(href, sessionCookie, ContentTypes.html)

        Assertions.assertThat(result.statusCode).isEqualTo(200)
    }
}