package org.vaccineimpact.orderlyweb.tests.unit_tests.templates

import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.ClassRule
import org.junit.Test
import org.vaccineimpact.orderlyweb.models.Document
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests
import org.vaccineimpact.orderlyweb.tests.unit_tests.templates.rules.FreemarkerTestRule
import org.vaccineimpact.orderlyweb.viewmodels.Breadcrumb
import org.vaccineimpact.orderlyweb.viewmodels.DefaultViewModel
import org.vaccineimpact.orderlyweb.viewmodels.DocumentsViewModel

class DocumentsPageTests : TeamcityTests()
{
    private val testDefaultModel = DefaultViewModel(true, "username",
            isReviewer = false,
            isAdmin = false,
            breadcrumbs = listOf(Breadcrumb("name", "url")))

    companion object
    {
        @ClassRule
        @JvmField
        val template = FreemarkerTestRule("documents.ftl")
    }

    @Test
    fun `renders list of docs`()
    {
        val leaf1 = Document("file.csv", "some/file.csv", true, listOf())
        val leaf2 = Document("empty", "some/empty/", false, listOf())
        val leaf3 = Document("file.csv", "some/path/file.doc", true, listOf())
        val root1 = Document("root", "root/", false, listOf())
        val root2 = Document("some", "some/", false,
                listOf(leaf1, leaf2, Document("path", "some/path/", false, listOf(leaf3))))

        val viewModel = DocumentsViewModel(listOf(root1, root2), testDefaultModel)
        val doc = template.jsoupDocFor(viewModel)

        val topLevelMenu = doc.selectFirst("ul")
        assertThat(topLevelMenu.select("li")[0].selectFirst("span").text()).isEqualTo("root")
        assertThat(topLevelMenu.select("li")[1].selectFirst("span").text()).isEqualTo("some")

        // first root node has no children
        assertThat(topLevelMenu.selectFirst("li").select("ul").count()).isEqualTo(0)

        val secondLevelMenu = topLevelMenu.select("li")[1].select("ul")
        assertThat(secondLevelMenu.select("li")[0].selectFirst("a").text()).isEqualTo("file.csv")
        assertThat(secondLevelMenu.select("li")[0].selectFirst("a").attr("href"))
                .isEqualTo("http://localhost:8888/project-docs/some/file.csv")
        assertThat(secondLevelMenu.select("li")[1].selectFirst("span").text()).isEqualTo("empty")
        assertThat(secondLevelMenu.select("li")[2].selectFirst("span").text()).isEqualTo("path")

        val thirdLevelMenu = secondLevelMenu.select("li")[2].selectFirst("ul")
        assertThat(thirdLevelMenu.selectFirst("li").selectFirst("a").text()).isEqualTo("file.csv")
        assertThat(thirdLevelMenu.selectFirst("li").selectFirst("a").attr("href"))
                .isEqualTo("http://localhost:8888/project-docs/some/path/file.doc")
    }
}