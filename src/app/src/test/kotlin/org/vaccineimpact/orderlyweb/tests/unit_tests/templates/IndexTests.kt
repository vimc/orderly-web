package org.vaccineimpact.orderlyweb.tests.unit_tests.templates

import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.ClassRule
import org.junit.Test
import org.vaccineimpact.orderlyweb.tests.unit_tests.templates.rules.FreemarkerTestRule
import org.vaccineimpact.orderlyweb.viewmodels.DefaultViewModel
import org.vaccineimpact.orderlyweb.viewmodels.DownloadableFileViewModel
import org.vaccineimpact.orderlyweb.viewmodels.IndexViewModel
import org.vaccineimpact.orderlyweb.viewmodels.PinnedReportViewModel

class IndexTests
{
    companion object
    {
        @ClassRule
        @JvmField
        val template = FreemarkerTestRule("index.ftl")
    }

    private fun testModelLink(isRunner: Boolean): IndexViewModel
    {
        return IndexViewModel(mock(), listOf(), listOf(), listOf(), listOf(), false, false, null, isRunner)
    }

    @Test
    fun `renders correctly`()
    {
        val testModel = IndexViewModel(mock(), listOf(), listOf(), listOf(), listOf(), true, false, null, false)

        val doc = template.jsoupDocFor(testModel)
        val breadcrumbs = doc.select(".crumb-item")
        assertThat(breadcrumbs.count()).isEqualTo(1)
        assertThat(breadcrumbs.first().selectFirst("a").text()).isEqualTo("Main menu")
        assertThat(breadcrumbs.first().selectFirst("a").attr("href")).isEqualTo("http://localhost:8888")

        assertThat(doc.select("h1.reports-list").text()).isEqualTo("Find a report")
        assertThat(doc.select("table#reports-table").count()).isEqualTo(1)
    }

    @Test
    fun `renders link to project docs if user has permission to see them`()
    {
        val testModel = IndexViewModel(mock(), listOf(), listOf(), listOf(), listOf(), true, false, null, false)
        val doc = template.jsoupDocFor(testModel)
        val docsLink = doc.selectFirst(".btn-link")
        assertThat(docsLink.selectFirst("a").text()).isEqualTo("View project documentation")
        assertThat(docsLink.selectFirst("a").attr("href")).isEqualTo("http://localhost:8888/project-docs")
    }

    @Test
    fun `does not render link to project docs if user does not have permission to see them`()
    {
        val testModel = IndexViewModel(mock(), listOf(), listOf(), listOf(), listOf(), false, false, null, false)
        val doc = template.jsoupDocFor(testModel)
        val docsLink = doc.select(".btn-link")
        assertThat(docsLink.count()).isEqualTo(0)
    }

    @Test
    fun `it does not render run workflow and run report links if user does not have permission to see them`()
    {
        val testModel = testModelLink(false)
        val doc = template.jsoupDocFor(testModel)
        val docsLink = doc.select(".btn-link")
        assertThat(docsLink.count()).isEqualTo(0)
    }

    @Test
    fun `it renders run report link if user has permission to see it`()
    {
        val testModel = testModelLink(true)
        val doc = template.jsoupDocFor(testModel)
        val docsLink = doc.selectFirst(".btn-link")
        assertThat(docsLink.selectFirst("a").text()).isEqualTo("Run a report")
        assertThat(docsLink.selectFirst("a").attr("href")).isEqualTo("http://localhost:8888/run-report")
    }

    @Test
    fun `renders pinned reports correctly`()
    {
        val testModel = IndexViewModel(mock(), listOf(), listOf(), listOf(
                PinnedReportViewModel("report1", "version1", "display1", "date1",
                        DownloadableFileViewModel("zip file 1", "zip file url 1", 1)),
                PinnedReportViewModel("report2", "version2", "display2", "date2",
                        DownloadableFileViewModel("zip file 2", "zip file url 2", 1))
        ), listOf(), true, false, null, false)

        val doc = template.jsoupDocFor(testModel)

        val header = doc.select("h1.pinned-reports")
        assertThat(header.count()).isEqualTo(1)
        assertThat(header[0].text()).isEqualTo("Pinned Reports")

        val pinnedReports = doc.select("#pinned-reports div.card")
        assertThat(pinnedReports.count()).isEqualTo(2)

        assertThat(pinnedReports[0].selectFirst("div.card-header a").text()).isEqualTo("display1")
        assertThat(pinnedReports[0].selectFirst("div.card-header a").attr("href"))
                .isEqualTo("http://localhost:8888/report/report1/version1/")
        assertThat(pinnedReports[0].selectFirst("div.card-header div.text-muted").text())
                .isEqualTo("Updated: date1")
        assertThat(pinnedReports[0].select("div.card-body a").text()).isEqualTo("Download latest")
        assertThat(pinnedReports[0].select("div.card-body a").attr("href"))
                .isEqualTo("zip file url 1")

        assertThat(pinnedReports[1].selectFirst("div.card-header a").text()).isEqualTo("display2")
        assertThat(pinnedReports[1].selectFirst("div.card-header a").attr("href"))
                .isEqualTo("http://localhost:8888/report/report2/version2/")
        assertThat(pinnedReports[1].selectFirst("div.card-header div.text-muted").text())
                .isEqualTo("Updated: date2")
        assertThat(pinnedReports[1].select("div.card-body a").text()).isEqualTo("Download latest")
        assertThat(pinnedReports[1].select("div.card-body a").attr("href"))
                .isEqualTo("zip file url 2")
    }

    @Test
    fun `renders correctly when no pinned reports`()
    {
        val testModel = IndexViewModel(mock(), listOf(), listOf(), listOf(), listOf(), true, false, null, false)

        val doc = template.jsoupDocFor(testModel)

        val header = doc.select("h1.pinned-reports")
        assertThat(header.count()).isEqualTo(0)

        val pinnedReports = doc.select("#pinned-reports div.card")
        assertThat(pinnedReports.count()).isEqualTo(0)
    }

    @Test
    fun `reviewers can see the status column`()
    {
        val defaultModel = DefaultViewModel(
                true,
                "username",
                isReviewer = true,
                isAdmin = false,
                isGuest = false,
                breadcrumbs = listOf(IndexViewModel.breadcrumb)
        )

        val testModel = IndexViewModel(listOf(), listOf(), listOf(), listOf("author", "requester"), true, false, null, defaultModel, false)
        val header = template.jsoupDocFor(testModel).selectFirst("thead tr")

        assertThat(header.select("th").count()).isEqualTo(7)
        assertThat(header.select("th")[0].selectFirst("label").text()).isEqualTo("Name")
        assertThat(header.select("th")[1].selectFirst("label").text()).isEqualTo("Version")
        assertThat(header.select("th")[2].selectFirst("label").text()).isEqualTo("Status")
        assertThat(header.select("th")[3].selectFirst("label").text()).isEqualTo("Tags")
        assertThat(header.select("th")[4].selectFirst("label").text()).isEqualTo("Parameter Values")
        assertThat(header.select("th")[5].selectFirst("label").text()).isEqualTo("Author")
        assertThat(header.select("th")[6].selectFirst("label").text()).isEqualTo("Requester")
    }

    @Test
    fun `non-reviewers cannot see the status column`()
    {
        val defaultModel = DefaultViewModel(true, "username", isReviewer = false,
                isAdmin = false, isGuest = false, breadcrumbs = listOf(IndexViewModel.breadcrumb))
        val testModel = IndexViewModel(listOf(), listOf(), listOf(), listOf("author", "requester"), true, false, null, defaultModel, false)

        val header = template.jsoupDocFor(testModel).selectFirst("thead tr")

        assertThat(header.select("th").count()).isEqualTo(6)
        assertThat(header.select("th")[0].selectFirst("label").text()).isEqualTo("Name")
        assertThat(header.select("th")[1].selectFirst("label").text()).isEqualTo("Version")
        assertThat(header.select("th")[2].selectFirst("label").text()).isEqualTo("Tags")
        assertThat(header.select("th")[3].selectFirst("label").text()).isEqualTo("Parameter Values")
        assertThat(header.select("th")[4].selectFirst("label").text()).isEqualTo("Author")
        assertThat(header.select("th")[5].selectFirst("label").text()).isEqualTo("Requester")

    }

    @Test
    fun `renders reportDisplayNames as js var if can set pinned reports`()
    {
        val defaultModel = DefaultViewModel(true, "username", isReviewer = false,
                isAdmin = false, isGuest = false, breadcrumbs = listOf(IndexViewModel.breadcrumb))
        val testModel = IndexViewModel(listOf(), listOf(), listOf(), listOf("author", "requester"), true,
                true, mapOf("r1" to "r1 display", "r2" to "r2 display"), defaultModel, false)

        val scriptEl = template.jsoupDocFor(testModel).getElementsByTag("script")[4].html()
        assertThat(scriptEl.indexOf("var reportDisplayNames = {\n" +
                "  \"r1\": \"r1 display\",\n" +
                "  \"r2\": \"r2 display\"\n" +
                "};")).isGreaterThan(-1)
    }

    @Test
    fun `does not render reportDisplayNames as js var if cannot set pinned reports`()
    {
        val defaultModel = DefaultViewModel(true, "username", isReviewer = false,
                isAdmin = false, isGuest = false, breadcrumbs = listOf(IndexViewModel.breadcrumb))
        val testModel = IndexViewModel(listOf(), listOf(), listOf(), listOf("author", "requester"), true,
                false, null, defaultModel, false)

        val scriptEl = template.jsoupDocFor(testModel).getElementsByTag("script")[2].html()
        assertThat(scriptEl.indexOf("var reportDisplayNames")).isEqualTo(-1);
    }

    @Test
    fun `renders set-global-pinned-reports component if can configure`()
    {
        val defaultModel = DefaultViewModel(true, "username", isReviewer = false,
                isAdmin = false, isGuest = false, breadcrumbs = listOf(IndexViewModel.breadcrumb))
        val testModel = IndexViewModel(listOf(), listOf(), listOf(), listOf("author", "requester"), true,
                true, mapOf("r1" to "r1 display", "r2" to "r2 display"), defaultModel, false)

        val component = template.jsoupDocFor(testModel).getElementsByTag("set-global-pinned-reports")
        assertThat(component.count()).isEqualTo(1)
        assertThat(component.attr(":current")).isEqualTo("currentPinnedReportNames")
        assertThat(component.attr(":available")).isEqualTo("reportDisplayNames")
    }

    @Test
    fun `does not render set-global-pinned-reports component if cannot configure`()
    {
        val defaultModel = DefaultViewModel(true, "username", isReviewer = false,
                isAdmin = false, isGuest = false, breadcrumbs = listOf(IndexViewModel.breadcrumb))
        val testModel = IndexViewModel(listOf(), listOf(), listOf(), listOf("author", "requester"), true,
                false, mapOf("r1" to "r1 display", "r2" to "r2 display"), defaultModel, false)

        val component = template.jsoupDocFor(testModel).getElementsByTag("set-global-pinned-reports")
        assertThat(component.count()).isEqualTo(0)
    }

    @Test
    fun `each column has a custom filter`()
    {
        val defaultModel = DefaultViewModel(true, "username", isReviewer = true,
                isAdmin = false, isGuest = false, breadcrumbs = listOf(IndexViewModel.breadcrumb))
        val testModel = IndexViewModel(listOf(), listOf(), listOf(), listOf("author", "requester"), true, false, null, defaultModel, false)

        val filters = template.jsoupDocFor(testModel).select("thead tr")[1]

        assertThat(filters.select("th").count()).isEqualTo(7)
        assertThat(filters.select("th")[0].selectFirst("input").id()).isEqualTo("name-filter")
        assertThat(filters.select("th")[1].selectFirst("input").id()).isEqualTo("version-filter")
        assertThat(filters.select("th")[2].selectFirst("select").id()).isEqualTo("status-filter")
        assertThat(filters.select("th")[3].selectFirst("select").id()).isEqualTo("tags-filter")
        assertThat(filters.select("th")[4].selectFirst("input").id()).isEqualTo("parameter-values-filter")
        assertThat(filters.select("th")[5].selectFirst("input").id()).isEqualTo("author-filter")
        assertThat(filters.select("th")[6].selectFirst("input").id()).isEqualTo("requester-filter")
    }

    @Test
    fun `tag column has a multiselect filter`()
    {
        val defaultModel = DefaultViewModel(true, "username", isReviewer = true,
                isAdmin = false, isGuest = false, breadcrumbs = listOf(IndexViewModel.breadcrumb))
        val testModel = IndexViewModel(listOf(), listOf("a", "b"), listOf(), listOf("author", "requester"), true, false, null, defaultModel, false)
        val filters = template.jsoupDocFor(testModel).select("thead tr")[1]

        val filterSelect = filters.select("th")[3].selectFirst("select")
        assertThat(filterSelect.id()).isEqualTo("tags-filter")
        assertThat(filterSelect.attr("multiple")).isEqualTo("multiple")
        assertThat(filterSelect.select("option").count()).isEqualTo(2)
        assertThat(filterSelect.select("option").map { it.attr("value") }).containsExactly("a", "b")
        assertThat(filterSelect.select("option").map { it.text() }).containsExactly("a", "b")
    }

    @Test
    fun `it renders run workflow link if user has permission to see it`()
    {
        val testModel = testModelLink(true)
        val doc = template.jsoupDocFor(testModel)
        val docsLink = doc.select(".btn-link")
        assertThat(docsLink.count()).isEqualTo(2)

        assertThat(docsLink[1].selectFirst("a").text()).isEqualTo("Run a workflow")
        assertThat(docsLink[1].selectFirst("a").attr("href")).isEqualTo("http://localhost:8888/run-workflow")
    }
}
