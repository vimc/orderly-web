package org.vaccineimpact.orderlyweb.tests.integration_tests.tests.web

import org.assertj.core.api.Assertions
import org.jsoup.Jsoup
import org.junit.jupiter.api.Test
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.tests.integration_tests.tests.IntegrationTest

class PublishReportsPageTests : IntegrationTest()
{
    private val reviewReports = setOf(ReifiedPermission("reports.review", Scope.Global()))
    @Test
    fun `only report reviewers can see the page`()
    {
        val url = "/publish-reports"
        assertWebUrlSecured(url, reviewReports)
    }

    @Test
    fun `correct page is served`()
    {
        val sessionCookie = webRequestHelper.webLoginWithMontagu(reviewReports)
        val response = webRequestHelper.requestWithSessionCookie("/publish-reports", sessionCookie)
        Assertions.assertThat(response.statusCode).isEqualTo(200)

        val page = Jsoup.parse(response.text)
        Assertions.assertThat(page.select("#publishReportsApp").count()).isEqualTo(1)
    }
}
