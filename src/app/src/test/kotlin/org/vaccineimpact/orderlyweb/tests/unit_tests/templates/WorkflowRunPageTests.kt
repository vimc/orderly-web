package org.vaccineimpact.orderlyweb.tests.unit_tests.templates

import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions
import org.junit.ClassRule
import org.junit.Test
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.tests.unit_tests.templates.rules.FreemarkerTestRule
import org.vaccineimpact.orderlyweb.viewmodels.WorkflowRunViewModel

class WorkflowRunPageTests
{
    companion object
    {
        @ClassRule
        @JvmField
        val template = FreemarkerTestRule("run-workflow-page.ftl")
    }

    private val testModel = WorkflowRunViewModel(mock<ActionContext>())
    private val doc = template.jsoupDocFor(testModel)

    @Test
    fun `renders page`()
    {
        Assertions.assertThat(doc.select("#runWorkflowTabsVueApp").count()).isEqualTo(1)
    }

    @Test
    fun `renders breadcrumbs correctly`()
    {
        val breadcrumbs = doc.select(".crumb-item")

        Assertions.assertThat(breadcrumbs.count()).isEqualTo(2)
        Assertions.assertThat(breadcrumbs[0].child(0).text()).isEqualTo("Main menu")
        Assertions.assertThat(breadcrumbs[0].child(0).attr("href")).isEqualTo("http://localhost:8888")
        Assertions.assertThat(breadcrumbs[1].child(0).text()).isEqualTo("Run a workflow")
        Assertions.assertThat(breadcrumbs[1].child(0).attr("href")).isEqualTo("http://localhost:8888/run-workflow")
    }

    @Test
    fun `renders script bundle`()
    {
        val script = doc.select("script")[4]
        Assertions.assertThat(script.attr("src")).isEqualTo("http://localhost:8888/js/runWorkflowTabs.bundle.js")
    }
}
