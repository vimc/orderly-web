package org.vaccineimpact.orderlyweb.tests.unit_tests.templates

import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.ClassRule
import org.junit.Test
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.tests.unit_tests.templates.rules.FreemarkerTestRule
import org.vaccineimpact.orderlyweb.viewmodels.AccessibilityViewModel

class AccessibilityTests
{
    companion object
    {
        @ClassRule
        @JvmField
        val template = FreemarkerTestRule("accessibility.ftl")
    }

    @Test
    fun `renders correctly when guest user allowed`()
    {
        val testViewModel = AccessibilityViewModel(mock<ActionContext>(), true)
        val doc = AccessibilityTests.template.jsoupDocFor(testViewModel)
        assertThat(doc.select("h1").text()).isEqualTo("Accessibility on Reporting portal")
        assertThat(doc.select("#access-loc").text())
                .isEqualTo("This statement applies to content published on http://localhost:8888")
        assertThat(doc.select("#access-regs").text())
                .startsWith("Technical information about this website’s accessibility")
        assertThat(doc.select("#access-email a").text())
                .isEqualTo("montagu-help@imperial.ac.uk")
        assertThat(doc.select("#access-email a").attr("href"))
                .isEqualTo("mailto:montagu-help@imperial.ac.uk")
        assertThat(doc.select("#access-enforce").text()).startsWith("Enforcement procedure")
    }

    @Test
    fun `renders correctly when guest user not allowed`()
    {
        val testViewModel = AccessibilityViewModel(mock<ActionContext>(), false)
        val doc = AccessibilityTests.template.jsoupDocFor(testViewModel)
        assertThat(doc.select("h1").text()).isEqualTo("Accessibility on Reporting portal")
        assertThat(doc.select("#access-loc").text())
                .isEqualTo("This statement applies to content published on http://localhost:8888")
        assertThat(doc.select("#access-regs")).isEmpty()
        assertThat(doc.select("#access-email a").text())
                .isEqualTo("montagu-help@imperial.ac.uk")
        assertThat(doc.select("#access-email a").attr("href"))
                .isEqualTo("mailto:montagu-help@imperial.ac.uk")
        assertThat(doc.select("#access-enforce")).isEmpty()
    }
}
