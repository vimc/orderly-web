package org.vaccineimpact.orderlyweb.tests.integration_tests.tests

import org.assertj.core.api.Assertions
import org.junit.Test

class RoutingTests: IntegrationTest() {

    @Test
    fun `can get url with or without trailing slash`()
    {
        var response = requestHelper.get("/reports/")
        assertSuccessful(response)

        response = requestHelper.get("/reports")
        assertSuccessful(response)
    }

    @Test
    fun `can get static files`()
    {
        val response = requestHelper.getWebPage("/css/style.css", "text/css")
        assertSuccessful(response)
        Assertions.assertThat(response.headers["content-type"]).isEqualTo("text/css")
    }

}