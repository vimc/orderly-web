package org.vaccineimpact.orderlyweb.tests.unit_tests.templates

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers
import org.jsoup.nodes.Document
import org.junit.ClassRule
import org.junit.Test
import org.pac4j.core.profile.CommonProfile
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.Config
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests
import org.vaccineimpact.orderlyweb.tests.unit_tests.templates.rules.FreemarkerTestRule
import org.vaccineimpact.orderlyweb.viewmodels.DefaultViewModel
import org.vaccineimpact.orderlyweb.viewmodels.IndexViewModel
import org.xmlmatchers.XmlMatchers
import javax.xml.transform.Source

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
        val testModel = IndexViewModel(mock(), listOf(), listOf())

        val doc = template.jsoupDocFor(testModel)

        val xml = template.xmlResponseFor(testModel)

        assertHeaderRenderedCorrectly(doc, xml)

        assertThat(doc.select(".logout").count()).isEqualTo(0) //should not show logged in view
        assertThat(doc.select("#content").count()).isEqualTo(1)
    }

    @Test
    fun `renders correctly when logged in`()
    {
        val mockContext = mock<ActionContext> {
            on { userProfile } doReturn CommonProfile().apply {
                id = "test.user"
            }
            on {
                hasPermission(ReifiedPermission("users.manage", Scope.Global()))
            } doReturn false
        }

        val testModel = IndexViewModel(mockContext, listOf(), listOf())

        val doc = template.jsoupDocFor(testModel)
        val xml = template.xmlResponseFor(testModel)

        assertHeaderRenderedCorrectly(doc, xml)

        assertThat(doc.selectFirst(".logout span").text()).isEqualTo("Logged in as test.user | Logout")
        assertThat(doc.selectFirst(".logout span a").attr("href")).isEqualTo("#")
        assertThat(doc.selectFirst(".logout span a").attr("onclick")).isEqualTo("logoutViaMontagu()")

        assertThat(doc.selectFirst(".logout span a").text()).isEqualTo("Logout")
        assertThat(doc.select("#content").count()).isEqualTo(1)

    }

    @Test
    fun `admins can see admin link`()
    {
        val mockContext = mock<ActionContext> {
            on { userProfile } doReturn CommonProfile().apply {
                id = "test.user"
            }
            on {
                hasPermission(ReifiedPermission("users.manage", Scope.Global()))
            } doReturn true
        }

        val testModel = IndexViewModel(mockContext, listOf(), listOf())

        val doc = template.jsoupDocFor(testModel)

        assertThat(doc.select(".logout span").count()).isEqualTo(2)
        assertThat(doc.selectFirst(".logout span").text()).isEqualTo("Admin |")
        assertThat(doc.selectFirst(".logout span a").attr("href")).isEqualTo("/admin")
    }

    @Test
    fun `non admins cannot see admin link`()
    {
        val mockContext = mock<ActionContext> {
            on { userProfile } doReturn CommonProfile().apply {
                id = "test.user"
            }
            on {
                hasPermission(ReifiedPermission("users.manage", Scope.Global()))
            } doReturn false
        }

        val testModel = IndexViewModel(mockContext, listOf(), listOf())

        val doc = template.jsoupDocFor(testModel)

        assertThat(doc.select(".logout span").count()).isEqualTo(1)
        assertThat(doc.selectFirst(".logout span").text()).isEqualTo("Logged in as test.user | Logout")
    }

    @Test
    fun `admins cannot see admin link if fine grained auth is turned off`()
    {
        val mockContext = mock<ActionContext> {
            on { userProfile } doReturn CommonProfile().apply {
                id = "test.user"
            }
            on {
                hasPermission(any())
            } doReturn true
        }
        val mockConfig = mock<Config> {
            on { authorizationEnabled } doReturn false
            on { get("app.name") } doReturn "appName"
            on { get("app.url") } doReturn "http://app"
            on { get("app.email") } doReturn "email"
            on { get("app.logo") } doReturn "logo.png"
            on { get("montagu.url") } doReturn "montagu"
        }

        val defaultModel = DefaultViewModel(mockContext, IndexViewModel.breadcrumb, appConfig = mockConfig)
        val testModel = IndexViewModel(listOf(), listOf(), defaultModel)

        val doc = template.jsoupDocFor(testModel)

        assertThat(doc.select(".logout span").count()).isEqualTo(1)
        assertThat(doc.selectFirst(".logout span").text()).isEqualTo("Logged in as test.user | Logout")
    }

    private fun assertHeaderRenderedCorrectly(doc: Document, xml: Source)
    {
        val appName = AppConfig()["app.name"]

        org.hamcrest.MatcherAssert.assertThat(xml,
                XmlMatchers.hasXPath("//link[@rel='icon']/@href", Matchers.equalTo("http://localhost:8888/favicon.ico")))

        org.hamcrest.MatcherAssert.assertThat(xml,
                XmlMatchers.hasXPath("//link[@rel='shortcut icon']/@href", Matchers.equalTo("http://localhost:8888/favicon.ico")))

        assertThat(doc.select("title").text()).isEqualTo(appName)
        assertThat(doc.select("header a").attr("href")).isEqualTo("/")
        assertThat(doc.select("header a img").attr("src")).isEqualTo("http://localhost:8888/img/logo/logo.png")
        assertThat(doc.select("header a img").attr("alt")).isEqualTo(appName)

        assertThat(doc.select(".site-title a").attr("href")).isEqualTo("http://localhost:8888")
        assertThat(doc.select(".site-title a").text()).isEqualTo(appName)

    }
}