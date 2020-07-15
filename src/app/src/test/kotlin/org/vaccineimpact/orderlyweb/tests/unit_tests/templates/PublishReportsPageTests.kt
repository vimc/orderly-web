package org.vaccineimpact.orderlyweb.tests.unit_tests.templates

import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.ClassRule
import org.junit.Test
import org.vaccineimpact.orderlyweb.Serializer
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

    private val testModel = PublishReportsViewModel(mock(),listOf())

    private val doc = template.jsoupDocFor(testModel)

    @Test
    fun `renders page`()
    {
        assertThat(doc.select("h2")[0].text()).isEqualTo("Latest drafts")
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
    fun `renders reports to script tag`()
    {
        val script = doc.select("script")[2]
        val reportsJson = Serializer.instance.gson.toJson(testModel.reportsWithDrafts)
        assertThat(script.html()).isEqualToIgnoringWhitespace("var reportsWithDrafts = ${reportsJson};")
    }
}