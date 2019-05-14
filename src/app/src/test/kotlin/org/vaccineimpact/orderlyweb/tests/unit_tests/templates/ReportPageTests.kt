package org.vaccineimpact.orderlyweb.tests.unit_tests.templates

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.ClassRule
import org.junit.Test
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.web.ReportController
import org.vaccineimpact.orderlyweb.models.ReportVersionDetails
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests
import org.vaccineimpact.orderlyweb.tests.unit_tests.templates.rules.FreemarkerTestRule
import org.xmlmatchers.XmlMatchers.hasXPath
import java.sql.Timestamp

//This will also test the partials which the report-page template includes

class ReportPageTests : TeamcityTests()
{
    companion object
    {
        @ClassRule
        @JvmField
        val template = FreemarkerTestRule("report-page.ftl")
    }

    private val testReport = ReportVersionDetails(name = "r1",
            displayName = "r1 display",
            id = "r1-v1",
            published = true,
            date = Timestamp(System.currentTimeMillis()).toInstant(),
            author = "an author",
            requester = "a requester",
            description = "description",
            artefacts = listOf(),
            resources = listOf(),
            dataHashes = mapOf())

    // Mock the wrapper serialization in the real model class
    private open class TestReportViewModel(open val reportJson: String,
                                           report: ReportVersionDetails,
                                           focalArtefactUrl: String?,
                                           isAdmin: Boolean,
                                           isRunner: Boolean,
                                           context: ActionContext) :
            ReportController.ReportViewModel(report, focalArtefactUrl, isAdmin, isRunner, context)

    private val mockModel = mock<TestReportViewModel> {
        on { appName } doReturn "testApp"
        on { report } doReturn testReport
        on { reportJson } doReturn "{}"
        on { isAdmin } doReturn false
        on { isRunner } doReturn false
        on { focalArtefactUrl } doReturn "/testFocalArtefactUrl"
    }

    @Test
    fun `renders outline correctly`()
    {
        val xmlResponse = template.xmlResponseFor(mockModel)

        assertThat(xmlResponse, hasXPath("//li[@class='nav-item'][1]/a[@role='tab']/text()",
                equalToCompressingWhiteSpace("Report")))

        assertThat(xmlResponse, hasXPath("//li[@class='nav-item'][2]/a[@role='tab']/text()",
                equalToCompressingWhiteSpace("Downloads")))

        assertThat(xmlResponse, hasXPath("//div[@class='tab-pane active' and @id='report']"))
        assertThat(xmlResponse, hasXPath("//div[@class='tab-pane' and @id='downloads']"))
    }

    @Test
    fun `renders report tab correctly`()
    {
        val xmlResponse = template.xmlResponseFor(mockModel)

        val xPathRoot = "//div[@id='report']"

        assertThat(xmlResponse, hasXPath("$xPathRoot/h1/text()",
                equalToCompressingWhiteSpace("r1 display")))
        assertThat(xmlResponse, hasXPath("$xPathRoot/p[1]/text()",
                equalToCompressingWhiteSpace("r1-v1")))

        assertThat(xmlResponse, hasXPath("$xPathRoot/iframe/@src", equalTo("/testFocalArtefactUrl")))
        assertThat(xmlResponse, hasXPath("$xPathRoot/div[@class='text-right']/a/text()",
                equalToCompressingWhiteSpace("View fullscreen")))
        assertThat(xmlResponse, hasXPath("$xPathRoot/div[@class='text-right']/a/@href", equalTo("/testFocalArtefactUrl")))
    }

    @Test
    fun `renders download tab correctly`()
    {
        val xmlResponse = template.xmlResponseFor(mockModel)

        val xPathRoot = "//div[@id='downloads']"

        assertThat(xmlResponse, hasXPath("$xPathRoot/h2/text()",
                equalToCompressingWhiteSpace("Downloads")))

    }

    @Test
    fun `admins see publish switch`()
    {
        val mockModel = mock<TestReportViewModel> {
            on { appName } doReturn "testApp"
            on { report } doReturn testReport
            on { reportJson } doReturn "{}"
            on { isAdmin } doReturn true
            on { focalArtefactUrl } doReturn "/testFocalArtefactUrl"
        }

        val htmlResponse = template.htmlPageResponseFor(mockModel)

        val publishSwitch = htmlResponse.getElementById("publishSwitchVueApp")
        Assertions.assertThat(publishSwitch).isNotNull()
    }

    @Test
    fun `non admins do not see publish switch`()
    {
        val mockModel = mock<TestReportViewModel> {
            on { appName } doReturn "testApp"
            on { report } doReturn testReport
            on { reportJson } doReturn "{}"
            on { isAdmin } doReturn false
            on { focalArtefactUrl } doReturn "/testFocalArtefactUrl"
        }

        val htmlResponse = template.htmlPageResponseFor(mockModel)

        val publishSwitch = htmlResponse.getElementById("publishSwitchVueApp")
        Assertions.assertThat(publishSwitch).isNull()
    }
}