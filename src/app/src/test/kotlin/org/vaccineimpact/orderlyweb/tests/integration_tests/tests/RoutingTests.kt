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

    @Test
    fun `can get API index`()
    {
        val response = requestHelper.get("/")
        assertJsonContentType(response)
        Assertions.assertThat(response.statusCode)
                .isEqualTo(200)
    }

    @Test
    fun `404 returns a page for non api endpoints`()
    {
        val response = requestHelper.getWebPage("/nonsense", "text/html")

        assertHtmlContentType(response)
        Assertions.assertThat(response.statusCode).isEqualTo(404)
    }

    @Test
    fun `404 returns json response for api endpoints`()
    {
        val response = requestHelper.get("/nonsense")

        assertJsonContentType(response)
        Assertions.assertThat(response.statusCode).isEqualTo(404)
    }

}