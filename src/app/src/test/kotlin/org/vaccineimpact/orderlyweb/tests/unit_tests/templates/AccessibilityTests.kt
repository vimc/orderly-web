package org.vaccineimpact.orderlyweb.tests.unit_tests.templates

import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.viewmodels.AccessibilityViewModel

class AccessibilityTests: FreeMarkerTest("accessibility.ftl")
{
    @Test
    fun `renders correctly`()
    {
        val testViewModel = AccessibilityViewModel(mock<ActionContext>())
        val doc = jsoupDocFor(testViewModel)
        assertThat(doc.select("h1").text()).isEqualTo("Accessibility on Reporting portal")
        assertThat(doc.select("#access-loc").text())
                .isEqualTo("This statement applies to content published on http://localhost:8888")
        assertThat(doc.select("#access-email a").text())
                .isEqualTo("montagu-help@imperial.ac.uk")
        assertThat(doc.select("#access-email a").attr("href"))
                .isEqualTo("mailto:montagu-help@imperial.ac.uk")
    }
}
