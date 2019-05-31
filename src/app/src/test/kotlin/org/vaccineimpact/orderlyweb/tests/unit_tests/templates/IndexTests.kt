package org.vaccineimpact.orderlyweb.tests.unit_tests.templates

import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.ClassRule
import org.junit.Test
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
        val testModel = IndexViewModel(mock(), listOf(), false)

        val doc = template.jsoupDocFor(testModel)
        val breadcrumbs = doc.select(".crumb-item")
        assertThat(breadcrumbs.count()).isEqualTo(1)
        assertThat(breadcrumbs.first().selectFirst("a").text()).isEqualTo("Main menu")
        assertThat(breadcrumbs.first().selectFirst("a").attr("href")).isEqualTo("/")

        assertThat(doc.select("h1").text()).isEqualTo("Find a report")
        assertThat(doc.select("table#reports-table").count()).isEqualTo(1)
    }

    @Test
    fun `reviewers can see the status column`()
    {
        val testModel = IndexViewModel(mock(), listOf(), true)
        val doc = template.jsoupDocFor(testModel)

        assertThat(doc.select("th").count()).isEqualTo(5)
        assertThat(doc.select("th")[0].text()).isEqualTo("Name")
        assertThat(doc.select("th")[1].text()).isEqualTo("Id")
        assertThat(doc.select("th")[2].text()).isEqualTo("Status")
        assertThat(doc.select("th")[3].text()).isEqualTo("Author")
        assertThat(doc.select("th")[4].text()).isEqualTo("Requester")
    }

    @Test
    fun `non-reviewers cannot see the status column`()
    {
        val testModel = IndexViewModel(mock(), listOf(), false)
        val doc = template.jsoupDocFor(testModel)

        assertThat(doc.select("th").count()).isEqualTo(4)
        assertThat(doc.select("th")[0].text()).isEqualTo("Name")
        assertThat(doc.select("th")[1].text()).isEqualTo("Id")
        assertThat(doc.select("th")[2].text()).isEqualTo("Author")
        assertThat(doc.select("th")[3].text()).isEqualTo("Requester")

    }
}