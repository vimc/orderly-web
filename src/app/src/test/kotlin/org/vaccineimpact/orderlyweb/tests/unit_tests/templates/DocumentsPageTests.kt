package org.vaccineimpact.orderlyweb.tests.unit_tests.templates

import org.assertj.core.api.Assertions
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.ClassRule
import org.junit.Test
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests
import org.vaccineimpact.orderlyweb.tests.unit_tests.templates.rules.FreemarkerTestRule
import org.vaccineimpact.orderlyweb.viewmodels.Breadcrumb
import org.vaccineimpact.orderlyweb.viewmodels.DefaultViewModel
import org.vaccineimpact.orderlyweb.viewmodels.DocumentsViewModel

class DocumentsPageTests : TeamcityTests()
{
    private val testDefaultModel = DefaultViewModel(
            loggedIn = true,
            user = "username",
            isReviewer = false,
            isAdmin = false,
            isGuest = false,
            breadcrumbs = listOf(Breadcrumb("name", "url")))

    companion object
    {
        @ClassRule
        @JvmField
        val template = FreemarkerTestRule("documents.ftl")
    }

    @Test
    fun `renders vue app`()
    {
        val viewModel = DocumentsViewModel(listOf(), false, testDefaultModel)
        val doc = template.jsoupDocFor(viewModel)

        val app = doc.selectFirst("#app")
        assertThat(app.html()).isEqualTo("<document-list :docs=\"docs\" :can-manage=\"canManage\"></document-list>")
    }

    @Test
    fun `renders canManage as js var`()
    {
        var viewModel = DocumentsViewModel(listOf(), false, testDefaultModel)
        var doc = template.jsoupDocFor(viewModel)
        var script = doc.getElementsByTag("script")[2].html().split("\n")[1]
        Assertions.assertThat(script.trim()).isEqualTo("var canManage = false;")

        viewModel = DocumentsViewModel(listOf(), true, testDefaultModel)
        doc = template.jsoupDocFor(viewModel)
        script = doc.getElementsByTag("script")[2].html().split("\n")[1]
        Assertions.assertThat(script.trim()).isEqualTo("var canManage = true;")
    }

}
