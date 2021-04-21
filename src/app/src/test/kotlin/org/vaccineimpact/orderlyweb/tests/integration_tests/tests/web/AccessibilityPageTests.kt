package org.vaccineimpact.orderlyweb.tests.integration_tests.tests.web

import org.assertj.core.api.Assertions
import org.jsoup.Jsoup
import org.junit.Test
import org.vaccineimpact.orderlyweb.tests.integration_tests.tests.IntegrationTest

class AccessibilityPageTests : IntegrationTest()
{
    @Test
    fun `correct page is served`()
    {
        val sessionCookie = webRequestHelper.webLoginWithMontagu()
        val response = webRequestHelper.requestWithSessionCookie("/accessibility", sessionCookie)
        Assertions.assertThat(response.statusCode).isEqualTo(200)

        val page = Jsoup.parse(response.text)
        Assertions.assertThat(page.selectFirst("h1").text()).isEqualTo("Accessibility on Reporting portal")
    }
}
