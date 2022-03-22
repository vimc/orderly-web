package org.vaccineimpact.orderlyweb.tests.unit_tests.templates

import com.nhaarman.mockito_kotlin.any
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
import org.vaccineimpact.orderlyweb.tests.unit_tests.templates.rules.FreemarkerTestRule
import org.vaccineimpact.orderlyweb.viewmodels.DefaultViewModel
import org.vaccineimpact.orderlyweb.viewmodels.IndexViewModel
import org.xmlmatchers.XmlMatchers
import javax.xml.transform.Source

class LayoutTests
{
    companion object
    {
        @ClassRule
        @JvmField
        // test the layout part of the home page
        val template = FreemarkerTestRule("index.ftl")
    }

    private fun testModel(): IndexViewModel
    {
        val mockContext = mock<ActionContext> {
            on { userProfile } doReturn CommonProfile().apply {
                id = "test.user"
            }
            on {
                hasPermission(ReifiedPermission("users.manage", Scope.Global()))
            } doReturn false
        }

        return IndexViewModel(mockContext, listOf(), listOf(), listOf(), listOf(), true, false, null, false)
    }

    @Test
    fun `renders correctly when not logged in`()
    {
        val testModel = IndexViewModel(mock(), listOf(), listOf(), listOf(), listOf(), true, false, null, false)

        val doc = template.jsoupDocFor(testModel)

        val xml = template.xmlResponseFor(testModel)

        assertHeaderRenderedCorrectly(doc, xml)

        assertThat(doc.select("#logged-in").count()).isEqualTo(0) // should not show logged in view
        assertThat(doc.select("#content").count()).isEqualTo(1)
        val script = doc.getElementsByTag("script")[0].html()
        assertThat(script.trim()).isEqualTo("var appUrl = \"http://localhost:8888\"")
    }

    @Test
    fun `renders correctly when logged in`()
    {
        val testModel = testModel()
        val doc = template.jsoupDocFor(testModel)
        val xml = template.xmlResponseFor(testModel)

        assertHeaderRenderedCorrectly(doc, xml)

        assertThat(doc.selectFirst(".nav-right #logged-in").text()).isEqualTo("Logged in as test.user")
        assertThat(doc.select("#logout-link").attr("href")).isEqualTo("#")
        assertThat(doc.select("#logout-link").attr("onclick")).isEqualTo("logoutViaMontagu()")

        assertThat(doc.select("#logout-link").text()).isEqualTo("Logout")
        assertThat(doc.select("#content").count()).isEqualTo(1)
        val script = doc.getElementsByTag("script")[0].html()
        assertThat(script.trim()).isEqualTo("var appUrl = \"http://localhost:8888\"")
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

        val testModel = IndexViewModel(mockContext, listOf(), listOf(), listOf(), listOf(), true, false, null, false)

        val doc = template.jsoupDocFor(testModel)

        assertThat(doc.select(".nav-right a.dropdown-item").count()).isEqualTo(2)
        assertThat(doc.selectFirst(".nav-right a.dropdown-item").text()).isEqualTo("Manage access")
        assertThat(doc.selectFirst(".nav-right a.dropdown-item").attr("href"))
                .isEqualTo("http://localhost:8888/manage-access")
    }

    @Test
    fun `non admins cannot see admin link`()
    {
        val testModel = testModel()
        val doc = template.jsoupDocFor(testModel)

        assertThat(doc.select(".nav-right a.dropdown-item").count()).isEqualTo(1)
        assertThat(doc.selectFirst(".nav-right #logged-in").text()).isEqualTo("Logged in as test.user")
    }

    @Test
    fun `admins cannot see admin link if fine grained auth is turned off`()
    {
        val mockContext = mock<ActionContext> {
            on { userProfile } doReturn CommonProfile().apply {
                id = "test.user"
            }
            on { hasPermission(any()) } doReturn true
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
        val testModel = IndexViewModel(listOf(), listOf(), listOf(), listOf(), true, false, null, defaultModel, false)

        val doc = template.jsoupDocFor(testModel)

        assertThat(doc.select(".nav-right a.dropdown-item").count()).isEqualTo(2)
        assertThat(doc.selectFirst(".nav-right #logged-in").text()).isEqualTo("Logged in as test.user")
    }

    @Test
    fun `reviewers can see publish link`()
    {
        val mockContext = mock<ActionContext> {
            on { userProfile } doReturn CommonProfile().apply {
                id = "test.user"
            }
            on {
                hasPermission(ReifiedPermission("reports.review", Scope.Global()))
            } doReturn true
        }

        val testModel = IndexViewModel(mockContext, listOf(), listOf(), listOf(), listOf(), true, false, null, false)

        val doc = template.jsoupDocFor(testModel)

        assertThat(doc.select(".nav-right a.dropdown-item").count()).isEqualTo(2)
        assertThat(doc.selectFirst(".nav-right a.dropdown-item").text()).isEqualTo("Publish reports")
        assertThat(doc.selectFirst(".nav-right a.dropdown-item").attr("href"))
                .isEqualTo("http://localhost:8888/publish-reports")
    }

    @Test
    fun `non reviewers can see publish link if fine grained auth is turned off`()
    {
        val mockContext = mock<ActionContext> {
            on { userProfile } doReturn CommonProfile().apply {
                id = "test.user"
            }
            on {
                hasPermission(ReifiedPermission("reports.review", Scope.Global()))
            } doReturn false
        }

        val mockConfig = mock<Config> {
            on { authorizationEnabled } doReturn false
            on { get("app.name") } doReturn "appName"
            on { get("app.url") } doReturn "http://localhost:8888"
            on { get("app.logo") } doReturn "logo.png"
            on { get("montagu.url") } doReturn "montagu"
        }

        val defaultModel = DefaultViewModel(mockContext, IndexViewModel.breadcrumb, appConfig = mockConfig)
        val testModel = IndexViewModel(listOf(), listOf(), listOf(), listOf(), true, false, null, defaultModel, false)

        val doc = template.jsoupDocFor(testModel)

        assertThat(doc.select(".nav-right a.dropdown-item").count()).isEqualTo(2)
        assertThat(doc.selectFirst(".nav-right a.dropdown-item").text()).isEqualTo("Publish reports")
        assertThat(doc.selectFirst(".nav-right a.dropdown-item").attr("href"))
                .isEqualTo("http://localhost:8888/publish-reports")
    }

    @Test
    fun `non reviewers cannot see publish link if fine grained auth is turned on`()
    {
        val mockContext = mock<ActionContext> {
            on { userProfile } doReturn CommonProfile().apply {
                id = "test.user"
            }
            on {
                hasPermission(ReifiedPermission("reports.review", Scope.Global()))
            } doReturn false
        }

        val testModel = IndexViewModel(mockContext, listOf(), listOf(), listOf(), listOf(), true, false, null, false)

        val doc = template.jsoupDocFor(testModel)

        assertThat(doc.select(".nav-right a.dropdown-item").count()).isEqualTo(1)
        assertThat(doc.selectFirst(".nav-right a.dropdown-item").text()).isEqualTo("Logout")
    }

    private fun assertHeaderRenderedCorrectly(doc: Document, xml: Source)
    {
        val appName = AppConfig()["app.name"]

        org.hamcrest.MatcherAssert.assertThat(xml,
                XmlMatchers.hasXPath("//link[@rel='icon']/@href",
                        Matchers.equalTo("http://localhost:8888/favicon.ico")))

        org.hamcrest.MatcherAssert.assertThat(xml,
                XmlMatchers.hasXPath("//link[@rel='shortcut icon']/@href",
                        Matchers.equalTo("http://localhost:8888/favicon.ico")))

        assertThat(doc.select("title").text()).isEqualTo(appName)
        assertThat(doc.select("header a").attr("href")).isEqualTo("/")
        assertThat(doc.select("header a img").attr("src")).isEqualTo("http://localhost:8888/img/logo/logo.png")
        assertThat(doc.select("header a img").attr("alt")).isEqualTo(appName)

        assertThat(doc.select(".site-title a").attr("href")).isEqualTo("http://localhost:8888")
        assertThat(doc.select(".site-title a").text()).isEqualTo(appName)

        assertThat(doc.select(".nav-right #accessibility-link").text()).isEqualTo("Accessibility")
        assertThat(doc.select(".nav-right #accessibility-link").attr("href"))
                .isEqualTo("http://localhost:8888/accessibility")
    }
}
