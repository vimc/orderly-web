package org.vaccineimpact.orderlyweb.tests.unit_tests.templates

import org.assertj.core.api.Assertions
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.Test
import org.vaccineimpact.orderlyweb.viewmodels.Breadcrumb
import org.vaccineimpact.orderlyweb.viewmodels.DefaultViewModel
import org.vaccineimpact.orderlyweb.viewmodels.DocumentsViewModel

class DocumentsPageTests: FreeMarkerTest("documents.ftl")
{
    private val testDefaultModel = DefaultViewModel(
            loggedIn = true,
            user = "username",
            isReviewer = false,
            isAdmin = false,
            isGuest = false,
            breadcrumbs = listOf(Breadcrumb("name", "url")))

    @Test
    fun `renders vue app`()
    {
        val viewModel = DocumentsViewModel(listOf(), false, testDefaultModel)
        val doc = jsoupDocFor(viewModel)

        val app = doc.selectFirst("#app")
        assertThat(app.html()).isEqualTo("<document-page :can-manage=\"canManage\"></document-page>")
    }

    @Test
    fun `renders canManage as js var`()
    {
        var viewModel = DocumentsViewModel(listOf(), false, testDefaultModel)
        var doc = jsoupDocFor(viewModel)
        var script = doc.getElementsByTag("script")[4].html().split("\n")[1]
        Assertions.assertThat(script.trim()).isEqualTo("var canManage = false;")

        viewModel = DocumentsViewModel(listOf(), true, testDefaultModel)
        doc = jsoupDocFor(viewModel)
        script = doc.getElementsByTag("script")[4].html().split("\n")[1]
        Assertions.assertThat(script.trim()).isEqualTo("var canManage = true;")
    }

}
