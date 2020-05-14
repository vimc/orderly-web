package org.vaccineimpact.orderlyweb.tests.unit_tests.templates

import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions
import org.junit.ClassRule
import org.junit.Test
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests
import org.vaccineimpact.orderlyweb.tests.unit_tests.templates.rules.FreemarkerTestRule
import org.vaccineimpact.orderlyweb.viewmodels.RunReportViewModel

class RunReportPageTests : TeamcityTests()
{
    companion object
    {
        @ClassRule
        @JvmField
        val template = FreemarkerTestRule("run-report-page.ftl")
    }

    private val testModel = RunReportViewModel(mock<ActionContext>())

    @Test
    fun `renders outline correctly`()
    {
        val doc = RunReportPageTests.template.jsoupDocFor(testModel)

        Assertions.assertThat(doc.select(".nav-item")[0].text()).isEqualTo("Run a report")
        Assertions.assertThat(doc.select(".nav-item")[1].text()).isEqualTo("Report logs")

        Assertions.assertThat(doc.selectFirst("#run-tab").hasClass("tab-pane active pt-4 pt-md-1")).isTrue()
        Assertions.assertThat(doc.selectFirst("#logs-tab").hasClass("tab-pane pt-4 pt-md-1")).isTrue()
    }

    @Test
    fun `renders breadcrumbs correctly`()
    {
        val doc = RunReportPageTests.template.jsoupDocFor(testModel)
        val breadcrumbs = doc.select(".crumb-item")

        Assertions.assertThat(breadcrumbs.count()).isEqualTo(2)
        Assertions.assertThat(breadcrumbs[0].child(0).text()).isEqualTo("Main menu")
        Assertions.assertThat(breadcrumbs[0].child(0).attr("href")).isEqualTo("http://localhost:8888")
        Assertions.assertThat(breadcrumbs[1].child(0).text()).isEqualTo("Run a report")
        Assertions.assertThat(breadcrumbs[1].child(0).attr("href")).isEqualTo("http://localhost:8888/run-report")
    }

    @Test
    fun `renders run tab correctly`()
    {
        val doc = RunReportPageTests.template.jsoupDocFor(testModel)
        val tab = doc.select("#run-tab")

        Assertions.assertThat(tab.select("h2").text()).isEqualToIgnoringWhitespace("Run a report")
        Assertions.assertThat(tab.select("#runReportVueApp").select("run-report").count()).isEqualTo(1)

    }

    @Test
    fun `renders logs tab correctly`()
    {
        val doc = RunReportPageTests.template.jsoupDocFor(testModel)
        val tab = doc.select("#logs-tab")

        Assertions.assertThat(tab.select("h2").text()).isEqualToIgnoringWhitespace("Report logs")
        Assertions.assertThat(tab.select("p").text()).isEqualToIgnoringWhitespace("Report logs coming soon!")
    }
}