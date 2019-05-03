package org.vaccineimpact.orderlyweb.tests.unit_tests.templates

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.ClassRule
import org.junit.Test
import org.vaccineimpact.orderlyweb.app_start.WebErrorHandler
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests
import org.vaccineimpact.orderlyweb.tests.unit_tests.templates.rules.FreemarkerTestRule
import org.xmlmatchers.XmlMatchers.hasXPath

class _401Tests: TeamcityTests()
{
    companion object
    {
        @ClassRule
        @JvmField
        val template = FreemarkerTestRule("401.ftl")
    }

    @Test
    fun `renders correctly for Montagu auth`()
    {
        val mockModel = mock<WebErrorHandler.ErrorViewModel> {
            on { appName } doReturn "testApp"
            on { authProvider } doReturn "montagu"
        }

        val xmlResponse = template.xmlResponseFor(mockModel)

        assertThat(xmlResponse, hasXPath("//h1/text()", equalToCompressingWhiteSpace("Login failed")))
        assertThat(xmlResponse, hasXPath("//p/text()",
                equalToCompressingWhiteSpace("We have not been able to successfully identify you as a Montagu user.")))
    }

    @Test
    fun `renders correctly for Github auth`()
    {
        val mockModel = mock<WebErrorHandler.ErrorViewModel> {
            on { appName } doReturn "testApp"
            on { authProvider } doReturn "github"
        }

        val xmlResponse = template.xmlResponseFor(mockModel)

        assertThat(xmlResponse, hasXPath("//h1/text()", equalToCompressingWhiteSpace("Login failed")))
        assertThat(xmlResponse, hasXPath("//p/text()",
                containsString("We have not been able to successfully identify you as a member of the app's configured Github org.")))

        assertThat(xmlResponse, hasXPath("//li[1]/a/text()",
                equalToCompressingWhiteSpace("GitHub organization approval for \"testApp\" has been requested")))
        assertThat(xmlResponse, hasXPath("//li[1]/a/@href",
                equalTo("https://help.github.com/en/articles/requesting-organization-approval-for-oauth-apps")))

        assertThat(xmlResponse, hasXPath("//li[2]/a/text()",
                equalToCompressingWhiteSpace("GitHub organization approval for \"testApp\" has been granted")))
        assertThat(xmlResponse, hasXPath("//li[2]/a/@href",
                equalTo("https://help.github.com/en/articles/approving-oauth-apps-for-your-organization")))
    }
}