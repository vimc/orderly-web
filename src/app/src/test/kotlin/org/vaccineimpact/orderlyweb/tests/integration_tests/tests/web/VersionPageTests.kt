package org.vaccineimpact.orderlyweb.tests.integration_tests.tests.web

import org.assertj.core.api.Assertions
import org.jsoup.Jsoup
import org.junit.Test
import org.vaccineimpact.orderlyweb.ContentTypes
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.Tables.*
import org.vaccineimpact.orderlyweb.db.fromJoinPath
import org.vaccineimpact.orderlyweb.db.joinPath
import org.vaccineimpact.orderlyweb.models.FilePurpose
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.tests.integration_tests.tests.IntegrationTest

class VersionPageTests : IntegrationTest()
{
    private val readReports = setOf(ReifiedPermission("reports.read", Scope.Global()))

    @Test
    fun `only report readers can see report version page`()
    {
        val (report, url) = getAnyReportPageUrl()
        assertWebUrlSecured(url,
                setOf(ReifiedPermission("reports.read", Scope.Specific("report", report))))
    }

    @Test
    fun `artefacts can be downloaded`()
    {
        val sessionCookie = webRequestHelper.webLoginWithMontagu(readReports)
        val (report, url) = getAnyReportPageUrl()
        val response = webRequestHelper.requestWithSessionCookie(url, sessionCookie)
        val page = Jsoup.parse(response.text)

        val firstArtefactHref = page.selectFirst("#artefacts a").attr("href")

        assertWebUrlSecured(firstArtefactHref,
                setOf(ReifiedPermission("reports.read", Scope.Specific("report", report))),
                ContentTypes.binarydata)
    }

    @Test
    fun `data can be downloaded`()
    {
        val (report, url) = getAnyReportPageUrl()
        val sessionCookie = webRequestHelper.webLoginWithMontagu(readReports)
        val response = webRequestHelper.requestWithSessionCookie(url, sessionCookie)
        val page = Jsoup.parse(response.text)

        val firstDataHref = page.selectFirst("#data-links a").attr("href")

        assertWebUrlSecured(firstDataHref,
                setOf(ReifiedPermission("reports.read", Scope.Specific("report", report))),
                contentType = ContentTypes.binarydata)
    }

    @Test
    fun `resources can be downloaded`()
    {
        val (report, url) = getAnyReportPageUrl()
        val sessionCookie = webRequestHelper.webLoginWithMontagu(readReports)
        val response = webRequestHelper.requestWithSessionCookie(url, sessionCookie)
        val page = Jsoup.parse(response.text)

        val firstResourceHref = page.selectFirst("#resources a").attr("href")

        assertWebUrlSecured(firstResourceHref,
                setOf(ReifiedPermission("reports.read", Scope.Specific("report", report))),
                contentType = ContentTypes.binarydata)
    }

    @Test
    fun `zip file can be downloaded`()
    {
        val (_, url) = getAnyReportPageUrl()
        val sessionCookie = webRequestHelper.webLoginWithMontagu(readReports)
        val response = webRequestHelper.requestWithSessionCookie(url, sessionCookie)
        val page = Jsoup.parse(response.text)

        val href = page.selectFirst("#zip-file a").attr("href")

        val result = webRequestHelper.requestWithSessionCookie(href, sessionCookie, ContentTypes.binarydata)

        Assertions.assertThat(result.statusCode).isEqualTo(200)
    }

    private fun getAnyReportPageUrl(): Pair<String, String>
    {
        val data = JooqContext().use {

            it.dsl.select(ORDERLYWEB_REPORT_VERSION_FULL.REPORT, ORDERLYWEB_REPORT_VERSION_FULL.ID)
                    .fromJoinPath(REPORT_VERSION_DATA,FILE_INPUT)
                    .join(ORDERLYWEB_REPORT_VERSION_FULL)
                    .on(REPORT_VERSION_DATA.REPORT_VERSION.eq(ORDERLYWEB_REPORT_VERSION_FULL.ID))
                    .where(ORDERLYWEB_REPORT_VERSION_FULL.PUBLISHED.eq(true))
                    .and(FILE_INPUT.FILE_PURPOSE.eq(FilePurpose.RESOURCE.toString()))
                    .fetchAny()
        }

        val report = data[ORDERLYWEB_REPORT_VERSION_FULL.REPORT]
        val version = data[ORDERLYWEB_REPORT_VERSION_FULL.ID]

        return Pair(report, "/report/$report/$version")
    }
}