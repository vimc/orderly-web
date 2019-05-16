package org.vaccineimpact.orderlyweb.tests.integration_tests.tests.web

import org.assertj.core.api.Assertions
import org.jooq.impl.DSL
import org.jsoup.Jsoup
import org.junit.Test
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.Tables.*
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.tests.integration_tests.tests.IntegrationTest

class IndexPageTests : IntegrationTest()
{
    private val readReports = setOf(ReifiedPermission("reports.read", Scope.Global()))
    private val reviewReports =  setOf(ReifiedPermission("reports.review", Scope.Global()))
    private val indexUrl = "/"

    @Test
    fun `report reviewers can see reports whose versions are all unpublished`()
    {
        val unpublishedReportName = getReportWithOnlyUnpublishedVersions()

        val sessionCookie = webRequestHelper.webLoginWithMontagu(reviewReports)
        val response = webRequestHelper.requestWithSessionCookie(indexUrl, sessionCookie)
        val page = Jsoup.parse(response.text)

        val reportNames = page.select("a").map{it -> it.text()}
        Assertions.assertThat(reportNames.contains(unpublishedReportName)).isTrue()
    }

    @Test
    fun `non report reviewers cannot see reports whose versions are all unpublished`()
    {
        val unpublishedReportName = getReportWithOnlyUnpublishedVersions()

        val sessionCookie = webRequestHelper.webLoginWithMontagu(readReports)
        val response = webRequestHelper.requestWithSessionCookie(indexUrl, sessionCookie)
        val page = Jsoup.parse(response.text)

        val reportNames = page.select("a").map{it -> it.text()}
        Assertions.assertThat(reportNames.contains(unpublishedReportName)).isFalse()
    }

    private fun getReportWithOnlyUnpublishedVersions(): String
    {
        val record = JooqContext().use {

            it.dsl.select(REPORT_VERSION.REPORT)
                    .from(REPORT_VERSION)
                    .groupBy(REPORT_VERSION.REPORT)
                    .having(DSL.max(REPORT_VERSION.PUBLISHED).eq(false))
                    .fetchAny()
        }

        return record[REPORT_VERSION.REPORT]
    }
}