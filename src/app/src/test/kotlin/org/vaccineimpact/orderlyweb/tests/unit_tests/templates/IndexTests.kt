package org.vaccineimpact.orderlyweb.tests.unit_tests.templates

import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.*
import org.junit.ClassRule
import org.junit.Test
import org.vaccineimpact.orderlyweb.models.Report
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests
import org.vaccineimpact.orderlyweb.tests.unit_tests.templates.rules.FreemarkerTestRule
import org.vaccineimpact.orderlyweb.viewmodels.IndexViewModel

class IndexTests : TeamcityTests()
{
    companion object
    {
        @ClassRule
        @JvmField
        val template = FreemarkerTestRule("index.ftl")
    }

    @Test
    fun `renders correctly`()
    {
        val testModel = IndexViewModel(mock(), listOf(Report("r1", "r1 display", "v1"),
                Report("r2", "r2 display", "v2")))

        val doc = template.jsoupDocFor(testModel)
        val breadCrumbs = doc.select(".breadcrumb-item")
        assertThat(breadCrumbs.count()).isEqualTo(1)
        assertThat(breadCrumbs.first().selectFirst("a").text()).isEqualTo("Main menu")
        assertThat(breadCrumbs.first().selectFirst("a").attr("href")).isEqualTo("/")

        assertThat(doc.select("h1").text()).isEqualTo("All reports")
        assertThat(doc.selectFirst("#content a").attr("href")).isEqualTo("reports/r1/v1")
        assertThat(doc.selectFirst("#content a").text()).isEqualTo("r1")
        assertThat(doc.select("#content a")[1].attr("href")).isEqualTo("reports/r2/v2")
        assertThat(doc.select("#content a")[1].text()).isEqualTo("r2")

    }
}