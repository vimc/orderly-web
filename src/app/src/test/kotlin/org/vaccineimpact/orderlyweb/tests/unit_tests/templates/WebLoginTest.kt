package org.vaccineimpact.orderlyweb.tests.unit_tests.templates

import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions
import org.junit.ClassRule
import org.junit.Test
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.tests.unit_tests.templates.rules.FreemarkerTestRule
import org.vaccineimpact.orderlyweb.viewmodels.WebloginViewModel

class WebLoginTest
{
    companion object
    {
        @ClassRule
        @JvmField
        val template = FreemarkerTestRule("weblogin.ftl")
    }

    private val testModel = WebloginViewModel(mock<ActionContext>(), "/fakepath")
    private val doc = template.jsoupDocFor(testModel)

    @Test
    fun `renders page`()
    {
        Assertions.assertThat(doc.select("#external-login").count()).isEqualTo(1)
    }

    @Test
    fun `renders breadcrumbs correctly`()
    {
        val breadcrumbs = doc.select(".crumb-item")

        Assertions.assertThat(breadcrumbs.count()).isEqualTo(2)
        Assertions.assertThat(breadcrumbs[0].child(0).text()).isEqualTo("Main menu")
        Assertions.assertThat(breadcrumbs[0].child(0).attr("href")).isEqualTo("http://localhost:8888")
        Assertions.assertThat(breadcrumbs[1].child(0).text()).isEqualTo("Login")
        Assertions.assertThat(breadcrumbs[1].child(0).attr("href")).isEqualTo("http://localhost:8888/weblogin")
    }

    @Test
    fun `renders external link correctly`()
    {
        val link = doc.select(".login-link")

        Assertions.assertThat(link.count()).isEqualTo(1)
        Assertions.assertThat(link.attr("href"))
            .isEqualTo("http://localhost:8888/weblogin/external?requestedUrl=/fakepath")
        Assertions.assertThat(link.text()).isEqualTo("Log in with GitHub")
    }
}