package org.vaccineimpact.orderlyweb.tests.unit_tests.templates

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.jsoup.nodes.Document
import org.junit.ClassRule
import org.junit.Test
import org.pac4j.core.profile.CommonProfile
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests
import org.vaccineimpact.orderlyweb.tests.unit_tests.templates.rules.FreemarkerTestRule
import org.vaccineimpact.orderlyweb.viewmodels.IndexViewModel

class LayoutTests : TeamcityTests()
{
    companion object
    {
        @ClassRule
        @JvmField
        //test the layout part of the home page
        val template = FreemarkerTestRule("index.ftl")
    }

    @Test
    fun `renders correctly when not logged in`()
    {
        val testModel = IndexViewModel(mock(), listOf(), listOf(), true)

        val doc = template.jsoupDocFor(testModel)

        assertHeaderRenderedCorrectly(doc)

        assertThat(doc.select(".logout").count()).isEqualTo(0) //should not show logged in view
        assertThat(doc.select("#content").count()).isEqualTo(1)
    }

    @Test
    fun `renders correctly when logged in`()
    {
        val mockContext = mock<ActionContext> {
            on {userProfile} doReturn CommonProfile().apply {
                id = "test.user"
            }
        }

        val testModel = IndexViewModel(mockContext, listOf(), listOf(), true)

        val doc = template.jsoupDocFor(testModel)

        assertHeaderRenderedCorrectly(doc)

        assertThat(doc.selectFirst(".logout span").text()).isEqualTo("Logged in as test.user | Logout")
        assertThat(doc.selectFirst(".logout span a").attr("href")).isEqualTo("#")
        assertThat(doc.selectFirst(".logout span a").text()).isEqualTo("Logout")
        assertThat(doc.select("#content").count()).isEqualTo(1)

    }

    private fun assertHeaderRenderedCorrectly(doc: Document)
    {
        val appName = AppConfig()["app.name"]

        assertThat(doc.select("title").text()).isEqualTo(appName)
        assertThat(doc.select("header a").attr("href")).isEqualTo("/")
        assertThat(doc.select("header a img").attr("src")).isEqualTo("/img/logo/logo.png")
        assertThat(doc.select("header a img").attr("alt")).isEqualTo(appName)

        assertThat(doc.select(".site-title a").attr("href")).isEqualTo("/")
        assertThat(doc.select(".site-title a").text()).isEqualTo(appName)
    }
}