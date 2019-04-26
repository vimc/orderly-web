package org.vaccineimpact.orderlyweb.tests.unit_tests.templates

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalToIgnoringWhiteSpace
import org.junit.Rule
import org.junit.Test
import org.vaccineimpact.orderlyweb.app_start.WebErrorHandler
import org.vaccineimpact.orderlyweb.models.ErrorInfo
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests
import org.vaccineimpact.orderlyweb.tests.unit_tests.templates.rules.FreemarkerTestRule
import org.xmlmatchers.XmlMatchers.*

class ErrorTests: TeamcityTests()
{
    @get:Rule
    val template = FreemarkerTestRule("error.ftl")

    @Test
    fun `renders correctly`()
    {
        val mockModel = mock<WebErrorHandler.ErrorViewModel> {
            on { appName } doReturn "testApp"
            on { errors } doReturn listOf(ErrorInfo("400", "oops"), ErrorInfo("500", "try again"))
        }

        val xmlResponse = template.xmlResponseFor(mockModel)

        assertThat(xmlResponse, hasXPath("//h1/text()", equalToIgnoringWhiteSpace("Something went wrong")))
        assertThat(xmlResponse, hasXPath("//li[1]/text()", equalToIgnoringWhiteSpace("oops")))
        assertThat(xmlResponse, hasXPath("//li[2]/text()", equalToIgnoringWhiteSpace("try again")))
    }
}


