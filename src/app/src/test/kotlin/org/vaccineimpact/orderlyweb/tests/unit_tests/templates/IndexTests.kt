package org.vaccineimpact.orderlyweb.tests.unit_tests.templates

import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.ClassRule
import org.junit.Test
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests
import org.vaccineimpact.orderlyweb.tests.unit_tests.templates.rules.FreemarkerTestRule
import org.vaccineimpact.orderlyweb.viewmodels.DownloadableFileViewModel
import org.vaccineimpact.orderlyweb.viewmodels.IndexViewModel
import org.vaccineimpact.orderlyweb.viewmodels.PinnedReportViewModel

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
        val testModel = IndexViewModel(mock(), listOf(), listOf(), false)

        val doc = template.jsoupDocFor(testModel)
        val breadcrumbs = doc.select(".crumb-item")
        assertThat(breadcrumbs.count()).isEqualTo(1)
        assertThat(breadcrumbs.first().selectFirst("a").text()).isEqualTo("Main menu")
        assertThat(breadcrumbs.first().selectFirst("a").attr("href")).isEqualTo("http://localhost:8888//")

        assertThat(doc.select("h1.reports-list").text()).isEqualTo("Find a report")
        assertThat(doc.select("table#reports-table").count()).isEqualTo(1)
    }

    @Test
    fun `renders pinned reports correctly`()
    {
        val testModel = IndexViewModel(mock(), listOf(), listOf(
                PinnedReportViewModel("report1", "version1", "display1", "date1",
                    DownloadableFileViewModel("zip file 1", "zip file url 1")),
                PinnedReportViewModel("report2", "version2", "display2", "date2",
                        DownloadableFileViewModel("zip file 2", "zip file url 2"))
        ), false)

        val doc = template.jsoupDocFor(testModel)

        val header = doc.select("h1.pinned-reports")
        assertThat(header.count()).isEqualTo(1)
        assertThat(header[0].text()).isEqualTo("Pinned Reports")

        val pinnedReports = doc.select("#pinned-reports div.card")
        assertThat(pinnedReports.count()).isEqualTo(2)

        assertThat(pinnedReports[0].selectFirst("div.card-header a").text()).isEqualTo("display1")
        assertThat(pinnedReports[0].selectFirst("div.card-header a").attr("href"))
                .isEqualTo("/reports/report1/version1/")
        assertThat(pinnedReports[0].selectFirst("div.card-header div.text-muted").text())
                .isEqualTo("Updated: date1")
        assertThat(pinnedReports[0].select("div.card-body a").text()).isEqualTo("Download latest")
        assertThat(pinnedReports[0].select("div.card-body a").attr("href"))
                .isEqualTo("zip file url 1")

        assertThat(pinnedReports[1].selectFirst("div.card-header a").text()).isEqualTo("display2")
        assertThat(pinnedReports[1].selectFirst("div.card-header a").attr("href"))
                .isEqualTo("/reports/report2/version2/")
        assertThat(pinnedReports[1].selectFirst("div.card-header div.text-muted").text())
                .isEqualTo("Updated: date2")
        assertThat(pinnedReports[1].select("div.card-body a").text()).isEqualTo("Download latest")
        assertThat(pinnedReports[1].select("div.card-body a").attr("href"))
                .isEqualTo("zip file url 2")
    }

    @Test
    fun `renders correctly when no pinned reports`()
    {
        val testModel = IndexViewModel(mock(), listOf(), listOf(), false)

        val doc = template.jsoupDocFor(testModel)

        val header = doc.select("h1.pinned-reports")
        assertThat(header.count()).isEqualTo(0)

        val pinnedReports = doc.select("#pinned-reports div.card")
        assertThat(pinnedReports.count()).isEqualTo(0)
    }


    @Test
    fun `reviewers can see the status column`()
    {
        val testModel = IndexViewModel(mock(), listOf(), listOf(), true)
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
        val testModel = IndexViewModel(mock(), listOf(), listOf(), false)
        val doc = template.jsoupDocFor(testModel)

        assertThat(doc.select("th").count()).isEqualTo(4)
        assertThat(doc.select("th")[0].text()).isEqualTo("Name")
        assertThat(doc.select("th")[1].text()).isEqualTo("Id")
        assertThat(doc.select("th")[2].text()).isEqualTo("Author")
        assertThat(doc.select("th")[3].text()).isEqualTo("Requester")

    }
}