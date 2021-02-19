package org.vaccineimpact.orderlyweb.tests.unit_tests.templates

import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.ClassRule
import org.junit.Test
import org.vaccineimpact.orderlyweb.Serializer
import org.vaccineimpact.orderlyweb.models.RunReportMetadata
import org.vaccineimpact.orderlyweb.tests.unit_tests.templates.rules.FreemarkerTestRule
import org.vaccineimpact.orderlyweb.viewmodels.RunReportViewModel

class RunReportPageTests
{
    companion object
    {
        @ClassRule
        @JvmField
        val template = FreemarkerTestRule("run-report-page.ftl")
    }

    private val testModel = RunReportViewModel(mock(),
            RunReportMetadata(true, true,
                    mapOf("source" to listOf("support", "annex")),
                    listOf("internal", "published")),
            listOf("master", "dev"))

    private val doc = template.jsoupDocFor(testModel)

    @Test
    fun `renders outline correctly`()
    {
        assertThat(doc.select(".nav-item")[0].text()).isEqualTo("Run a report")
        assertThat(doc.select(".nav-item")[1].text()).isEqualTo("Report logs")

        assertThat(doc.selectFirst("#run-tab").hasClass("tab-pane active pt-4 pt-md-1")).isTrue()
        assertThat(doc.selectFirst("#logs-tab").hasClass("tab-pane pt-4 pt-md-1")).isTrue()
    }

    @Test
    fun `renders breadcrumbs correctly`()
    {
        val breadcrumbs = doc.select(".crumb-item")

        assertThat(breadcrumbs.count()).isEqualTo(2)
        assertThat(breadcrumbs[0].child(0).text()).isEqualTo("Main menu")
        assertThat(breadcrumbs[0].child(0).attr("href")).isEqualTo("http://localhost:8888")
        assertThat(breadcrumbs[1].child(0).text()).isEqualTo("Run a report")
        assertThat(breadcrumbs[1].child(0).attr("href")).isEqualTo("http://localhost:8888/run-report")
    }

    @Test
    fun `renders run tab correctly`()
    {
        val tab = doc.select("#run-tab")

        assertThat(tab.select("h2").text()).isEqualToIgnoringWhitespace("Run a report")

        val runReportComponent = tab.select("#runReportVueApp").select("run-report")
        assertThat(runReportComponent.attr(":metadata")).isEqualTo("runReportMetadata")
        assertThat(runReportComponent.attr(":git-branches")).isEqualTo("gitBranches")

    }

    @Test
    fun `renders logs tab correctly`()
    {
        val tab = doc.select("#logs-tab")

        assertThat(tab.select("h2").text()).isEqualToIgnoringWhitespace("Running report logs")
        val runReportTabsComponent = tab.select("#runReportTabsVueApp").select("run-report-tabs")
        assertThat(runReportTabsComponent.size).isEqualTo(1)
    }

    @Test
    fun `renders metadata and git branches to script tag`()
    {
        val script = doc.select("script")[4]
        val metadataJson = Serializer.instance.gson.toJson(testModel.runReportMetadata)
        assertThat(script.html()).isEqualToIgnoringWhitespace("var runReportMetadata = ${metadataJson};"
                + " var gitBranches = [ \"master\", \"dev\" ];")
    }
}
