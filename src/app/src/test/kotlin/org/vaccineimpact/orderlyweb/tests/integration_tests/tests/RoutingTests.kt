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
    fun `can get index`()
    {
        val response = requestHelper.getWebPage("/")
        Assertions.assertThat(response.statusCode)
                .isEqualTo(200)
        Assertions.assertThat(response.headers["content-type"]).contains("text/html")
    }

    @Test
    fun `gets 404 page if accept header contains html`()
    {
        val response = requestHelper.getWebPage("/nonsense", "text/html")

        assertHtmlContentType(response)
        Assertions.assertThat(response.statusCode).isEqualTo(404)
    }

    @Test
    fun `gets 404 page if accept header contains *`()
    {
        val response = requestHelper.getWebPage("/nonsense", "*/*")

        assertHtmlContentType(response)
        Assertions.assertThat(response.statusCode).isEqualTo(404)
    }


    @Test
    fun `gets json response if accept header does not contain html or *`()
    {
        val response = requestHelper.get("/nonsense")

        assertJsonContentType(response)
        Assertions.assertThat(response.statusCode).isEqualTo(404)
    }

}