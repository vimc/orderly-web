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
    fun `renders run report tabs correctly`()
    {
        val runReportTabsComponent = doc.select("#runReportTabsVueApp").select("run-report-tabs")
        assertThat(runReportTabsComponent.attr(":metadata")).isEqualTo("runReportMetadata")
        assertThat(runReportTabsComponent.attr(":initial-git-branches")).isEqualTo("gitBranches")
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
