package org.vaccineimpact.orderlyweb.tests.unit_tests.templates

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.Rule
import org.junit.Test
import org.vaccineimpact.orderlyweb.app_start.WebErrorHandler
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests
import org.vaccineimpact.orderlyweb.tests.unit_tests.templates.rules.FreemarkerTestRule
import org.xmlmatchers.XmlMatchers

class _404Tests: TeamcityTests()
{
    @get:Rule
    val template = FreemarkerTestRule("404.ftl")

    @Test
    fun `renders correctly`()
    {
        val mockModel = mock<WebErrorHandler.ErrorViewModel> {
            on { appName } doReturn "testApp"
            on { appEmail } doReturn "test@test.com"
        }

        val xmlResponse = template.xmlResponseFor(mockModel)

        assertThat(xmlResponse, XmlMatchers.hasXPath("//h1/text()", equalToIgnoringWhiteSpace("Page not found")))
        assertThat(xmlResponse, XmlMatchers.hasXPath("//li[1]/text()",
                equalToIgnoringWhiteSpace("Click back in your browser to return to the previous page")))

        assertThat(xmlResponse, XmlMatchers.hasXPath("//li[2]/text()",
                equalToIgnoringWhiteSpace("Return to")))
        assertThat(xmlResponse, XmlMatchers.hasXPath("//li[2]/a/text()",
                equalToIgnoringWhiteSpace("the main menu")))
        assertThat(xmlResponse, XmlMatchers.hasXPath("//li[2]/a/@href",
                equalToIgnoringWhiteSpace("/reports")))

        assertThat(xmlResponse, XmlMatchers.hasXPath("//li[3]/text()",
                equalToIgnoringWhiteSpace("If you are sure this page should exist, please")))
        assertThat(xmlResponse, XmlMatchers.hasXPath("//li[3]/a/text()",
                equalToIgnoringWhiteSpace("let us know")))
        assertThat(xmlResponse, XmlMatchers.hasXPath("//li[3]/a/@href",
                equalToIgnoringWhiteSpace("mailto:test@test.com")))
    }
}


