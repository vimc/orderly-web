package org.vaccineimpact.orderlyweb.tests.integration_tests.tests.web

import com.github.salomonbrys.kotson.fromJson
import com.google.gson.JsonArray
import com.google.gson.JsonParser
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.junit.Test
import org.vaccineimpact.orderlyweb.ContentTypes
import org.vaccineimpact.orderlyweb.Serializer
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
    fun `only reports the user has permission to read are shown`()
    {
        // with single report permission
        var sessionCookie = webRequestHelper.webLoginWithMontagu(setOf(ReifiedPermission("reports.read",
                Scope.Specific("report", "minimal"))))
        var response = webRequestHelper.requestWithSessionCookie("/", sessionCookie)

        var page = Jsoup.parse(response.text)

        var reports = getReportsJsonFromPage(page)
        assertThat(reports.count()).isEqualTo(2)

        // with global permission
        sessionCookie = webRequestHelper.webLoginWithMontagu(setOf(ReifiedPermission("reports.read",
                Scope.Global())))
        response = webRequestHelper.requestWithSessionCookie("/", sessionCookie)

        page = Jsoup.parse(response.text)

        reports = getReportsJsonFromPage(page)
        assertThat(reports.count()).isEqualTo(13)
    }

    @Test
    fun `unauthenticated users cannot get index page`()
    {
        val response = webRequestHelper.getWebPage("/")

        val page = Jsoup.parse(response.text)
        assertThat(page.selectFirst(".siteTitle").text()).isEqualTo("Montagu") // user has been redirected to login
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

    private fun getReportsJsonFromPage(page: Document): JsonArray
    {
        val reportsTag = page.getElementsByTag("script")[2].html()
                .split("var rawReports = ")[1];
        val reportsString =  reportsTag.split(";")[0]
        return JsonParser().parse(reportsString) as JsonArray
    }
}