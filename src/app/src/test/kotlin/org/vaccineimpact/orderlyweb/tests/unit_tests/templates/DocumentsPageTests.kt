package org.vaccineimpact.orderlyweb.tests.unit_tests.templates

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
    fun `renders vue app`()
    {
        val viewModel = DocumentsViewModel(listOf(), testDefaultModel)

        val doc = template.jsoupDocFor(viewModel)

        val app = doc.selectFirst("#app")
        assertThat(app.html()).isEqualTo("<document-list :docs=\"docs\"></document-list>")
    }
}
