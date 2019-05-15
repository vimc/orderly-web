package org.vaccineimpact.orderlyweb.tests.integration_tests.tests.web

import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import org.junit.Test
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.Tables.REPORT_VERSION
import org.vaccineimpact.orderlyweb.db.Tables.REPORT_VERSION_ARTEFACT
import org.vaccineimpact.orderlyweb.tests.integration_tests.tests.IntegrationTest

class ErrorPageTests : IntegrationTest()
{
    @Test
    fun `404 page details are correct`()
    {
        val response = webRequestHelper.getWebPage("/nonsense", "text/html")

        Assertions.assertThat(response.statusCode).isEqualTo(404)

        val doc = Jsoup.parse(response.text)
        assertOneBreadcrumbWithNoLink(doc, "Page not found")

        assertThat(doc.selectFirst("h1").text()).isEqualTo("Page not found")
        assertThat(doc.selectFirst("li").text()).isEqualTo("Click back in your browser to return to the previous page")

        assertThat(doc.select("li")[1].text()).isEqualTo("Return to the main menu")
        assertThat(doc.selectFirst("li a").text()).isEqualTo("the main menu")
        assertThat(doc.selectFirst("li a").attr("href")).isEqualTo("/reports")

        assertThat(doc.select("li")[2].text()).isEqualTo("If you are sure this page should exist, please let us know")
        assertThat(doc.select("li a")[1].text()).isEqualTo("let us know")
        assertThat(doc.select("li a")[1].attr("href")).isEqualTo("mailto:${AppConfig()["app.email"]}")
    }

    @Test
    fun `401 page details are correct`()
    {
        val response = webRequestHelper.getWithMontaguCookie("/login/", "bad-token", allowRedirects = true)

        Assertions.assertThat(response.statusCode).isEqualTo(401)

        val doc = Jsoup.parse(response.text)
        assertThat(doc.selectFirst("h1").text()).isEqualTo("Login failed")
        assertThat(doc.selectFirst("p").text()).isEqualTo("We have not been able to successfully identify you as a Montagu user.")

        assertOneBreadcrumbWithNoLink(doc, "Login failed")
    }

    @Test
    fun `500 page details are correct`()
    {
        // set up db in bad state
        val (report, version) = JooqContext(enableForeignKeyConstraints = false).use {
            it.dsl.update(REPORT_VERSION_ARTEFACT)
                    .set(REPORT_VERSION_ARTEFACT.FORMAT, "BAD-FORMAT")
                    .execute()

            val report = it.dsl.selectFrom(REPORT_VERSION)
                    .where(REPORT_VERSION.PUBLISHED.eq(true))
                    .first()

            Pair(report[REPORT_VERSION.REPORT], report[REPORT_VERSION.ID])
        }

        val result = webRequestHelper.loginWithMontaguAndMakeRequest("/reports/$report/$version/")

        assertThat(result.statusCode).isEqualTo(500)
        val doc = Jsoup.parse(result.text)
        assertThat(doc.selectFirst("h1").text()).isEqualTo("Something went wrong")
        assertThat(doc.selectFirst("li").text()).contains("An unexpected error occurred. Please contact support")

        assertOneBreadcrumbWithNoLink(doc, "Something went wrong")
    }

    private fun assertOneBreadcrumbWithNoLink(doc: Document, expectedText: String)
    {
        val breadCrumbs = doc.select(".breadcrumb-item")
        assertThat(breadCrumbs.count()).isEqualTo(1)
        assertThat(breadCrumbs.first().text()).isEqualTo(expectedText)
        assertThat(breadCrumbs.first().child(0).`is`("span"))
                .withFailMessage("Expected breadcrumb with null url to be a span").isTrue()

    }
}