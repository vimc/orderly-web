package org.vaccineimpact.orderlyweb.tests.integration_tests.tests.web

import org.assertj.core.api.Assertions
import org.jsoup.Jsoup
import org.junit.Test
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.tests.integration_tests.tests.IntegrationTest

class AdminPageTests : IntegrationTest()
{
    private val manageUsers = setOf(ReifiedPermission("users.manage", Scope.Global()))

    @Test
    fun `only user managers can see the page`()
    {
        val url = "/manage-access"
        assertWebUrlSecured(url, manageUsers)
    }

    @Test
    fun `correct page is served`()
    {
        val sessionCookie = webRequestHelper.webLoginWithMontagu(manageUsers)
        val response = webRequestHelper.requestWithSessionCookie("/manage-access", sessionCookie)
        Assertions.assertThat(response.statusCode).isEqualTo(200)

        val page = Jsoup.parse(response.text)
        Assertions.assertThat(page.selectFirst("#adminVueApp")).isNotNull()
    }
}
