package org.vaccineimpact.orderlyweb.tests.unit_tests.templates

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.Rule
import org.junit.Test
import org.vaccineimpact.orderlyweb.controllers.web.HomeController
import org.vaccineimpact.orderlyweb.models.Report
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests
import org.vaccineimpact.orderlyweb.tests.unit_tests.templates.rules.FreemarkerTestRule
import org.xmlmatchers.XmlMatchers.hasXPath

class IndexTests: TeamcityTests()
{
    @get:Rule
    val template = FreemarkerTestRule("index.ftl")

    @Test
    fun `renders correctly`()
    {
        val mockModel = mock<HomeController.IndexViewModel> {
            on { appName } doReturn "testApp"
            on { reports } doReturn listOf(Report("r1", "r1 display", "v1"),
                                            Report("r2", "r2 display", "v2"))
        }

        val xmlResponse = template.xmlResponseFor(mockModel)

        assertThat(xmlResponse, hasXPath("//h1/text()", equalToIgnoringWhiteSpace("All reports")))

        assertThat(xmlResponse, hasXPath("//div[@id='content']/a[1]/text()",
                equalToIgnoringWhiteSpace("r1")))
        assertThat(xmlResponse, hasXPath("//div[@id='content']/a[1]/@href", equalTo("reports/r1/v1")))

        assertThat(xmlResponse, hasXPath("//div[@id='content']/a[2]/text()",
                equalToIgnoringWhiteSpace("r2")))
        assertThat(xmlResponse, hasXPath("//div[@id='content']/a[2]/@href", equalTo("reports/r2/v2")))

    }
}