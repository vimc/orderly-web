package org.vaccineimpact.orderlyweb.tests.unit_tests.templates.vuex

import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.ClassRule
import org.junit.Test
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.tests.unit_tests.templates.rules.FreemarkerTestRule
import org.vaccineimpact.orderlyweb.viewmodels.vuex.RunReportViewModel


class RunReportPageTests
{
    companion object
    {
        @ClassRule
        @JvmField
        val template = FreemarkerTestRule("vuex-run-report-page.ftl")
    }

    private val testModel = RunReportViewModel(mock<ActionContext>())

    private val doc = template.jsoupDocFor(testModel)

    @Test
    fun `renders breadcrumbs correctly`()
    {
        val breadcrumbs = doc.select(".crumb-item")

        assertThat(breadcrumbs.count()).isEqualTo(2)
        assertThat(breadcrumbs[0].child(0).text()).isEqualTo("Main menu")
        assertThat(breadcrumbs[0].child(0).attr("href")).isEqualTo("http://localhost:8888")
        assertThat(breadcrumbs[1].child(0).text()).isEqualTo("Run a report")
        assertThat(breadcrumbs[1].child(0).attr("href")).isEqualTo("http://localhost:8888/vuex-run-report")
    }

    @Test
    fun `renders run report header correctly`()
    {
        val runReportComponent = doc.select("#vuexRunReportApp").select("h1")
        assertThat(runReportComponent.text()).isEqualTo("Vuex Run Report Page")
    }
}