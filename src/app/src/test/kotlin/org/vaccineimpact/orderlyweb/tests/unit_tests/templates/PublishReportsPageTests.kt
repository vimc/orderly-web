package org.vaccineimpact.orderlyweb.tests.unit_tests.templates

import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.ClassRule
import org.junit.Test
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests
import org.vaccineimpact.orderlyweb.tests.unit_tests.templates.rules.FreemarkerTestRule
import org.vaccineimpact.orderlyweb.viewmodels.PublishReportsViewModel

class PublishReportsPageTests : TeamcityTests()
{
    companion object
    {
        @ClassRule
        @JvmField
        val template = FreemarkerTestRule("publish-reports.ftl")
    }

    private val testModel = PublishReportsViewModel(mock<ActionContext>())

    private val doc = template.jsoupDocFor(testModel)

    @Test
    fun `renders page`()
    {
        assertThat(doc.select("#publishReportsApp").count()).isEqualTo(1)
    }

    @Test
    fun `renders breadcrumbs correctly`()
    {
        val breadcrumbs = doc.select(".crumb-item")

        assertThat(breadcrumbs.count()).isEqualTo(2)
        assertThat(breadcrumbs[0].child(0).text()).isEqualTo("Main menu")
        assertThat(breadcrumbs[0].child(0).attr("href")).isEqualTo("http://localhost:8888")
        assertThat(breadcrumbs[1].child(0).text()).isEqualTo("Publish reports")
        assertThat(breadcrumbs[1].child(0).attr("href")).isEqualTo("http://localhost:8888/publish-reports")
    }

    @Test
    fun `renders script bundle`()
    {
        val script = doc.select("script")[4]
        assertThat(script.attr("src")).isEqualTo("http://localhost:8888/js/publishReports.bundle.js")
    }
}
