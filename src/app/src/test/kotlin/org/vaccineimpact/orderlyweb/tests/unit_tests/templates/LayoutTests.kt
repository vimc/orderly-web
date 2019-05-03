package org.vaccineimpact.orderlyweb.tests.unit_tests.templates

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.ClassRule
import org.junit.Test
import org.vaccineimpact.orderlyweb.controllers.web.HomeController
import org.vaccineimpact.orderlyweb.models.Report
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests
import org.vaccineimpact.orderlyweb.tests.unit_tests.templates.rules.FreemarkerTestRule
import org.xmlmatchers.XmlMatchers.hasXPath

class LayoutTests: TeamcityTests()
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
        val mockModel = mock<HomeController.IndexViewModel> {
            on { appName } doReturn "testApp"
            on { reports } doReturn listOf<Report>() //not testing report rendering
            on { loggedIn } doReturn false
        }

        val xmlResponse = template.xmlResponseFor(mockModel)

        //header
        assertThat(xmlResponse, hasXPath("//head/title", equalToCompressingWhiteSpace("testApp")))
        assertThat(xmlResponse, hasXPath("//header/a/@href", equalTo("/")))
        assertThat(xmlResponse, hasXPath("//header/a/img/@src", equalTo("/img/logo.png")))
        assertThat(xmlResponse, hasXPath("//header/a/img/@alt", equalTo("testApp")))

        assertThat(xmlResponse, hasXPath("//header/div[@class='site-title']/a/@href",
                equalTo("/")))
        assertThat(xmlResponse, hasXPath("//header/div[@class='site-title']/a/text()",
                equalToCompressingWhiteSpace("testApp")))

        assertThat(xmlResponse, not(hasXPath("//header/div[@class='logout']"))) //should not show logged in view

        //content
        assertThat(xmlResponse, hasXPath("//div[@class='container-fluid pt-5']/div[@class='row']/div[@id='content']"))
    }

    @Test
    fun `renders correctly when logged in`()
    {
        val mockModel = mock<HomeController.IndexViewModel> {
            on { appName } doReturn "testApp"
            on { reports } doReturn listOf<Report>() //not testing report rendering
            on { loggedIn } doReturn true
            on { user } doReturn "testUser"
        }

        val xmlResponse = template.xmlResponseFor(mockModel)

        //header
        assertThat(xmlResponse, hasXPath("//head/title", equalToCompressingWhiteSpace("testApp")))
        assertThat(xmlResponse, hasXPath("//header/a/@href", equalTo("/")))
        assertThat(xmlResponse, hasXPath("//header/a/img/@src", equalTo("/img/logo.png")))

        assertThat(xmlResponse, hasXPath("//header/div[@class='site-title']/a/text()",
                equalToCompressingWhiteSpace("testApp")))

        assertThat(xmlResponse, hasXPath("//header/div[@class='logout']/span/text()",
                equalToCompressingWhiteSpace("Logged in as testUser |")))
        assertThat(xmlResponse, hasXPath("//header/div[@class='logout']/span/a/@href",
                equalTo("/logout")))
        assertThat(xmlResponse, hasXPath("//header/div[@class='logout']/span/a/text()",
                equalToCompressingWhiteSpace("Logout")))

        //content
        assertThat(xmlResponse, hasXPath("//div[@class='container-fluid pt-5']/div[@class='row']/div[@id='content']"))
    }
}