package org.vaccineimpact.orderlyweb.tests.unit_tests.templates

import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.ClassRule
import org.junit.Test
import org.vaccineimpact.orderlyweb.errors.BadConfigurationError
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests
import org.vaccineimpact.orderlyweb.tests.unit_tests.templates.rules.FreemarkerTestRule
import org.vaccineimpact.orderlyweb.viewmodels.ServerErrorViewModel

class _500Tests: TeamcityTests()
{
    companion object
    {
        @ClassRule
        @JvmField
        val template = FreemarkerTestRule("500.ftl")
    }

    @Test
    fun `renders correctly`()
    {
        val error = BadConfigurationError("TEST")
        val model = ServerErrorViewModel(error, mock())
        val doc = template.jsoupDocFor(model)

        assertThat(doc.selectFirst("h1").text()).isEqualTo("Something went wrong")
        assertThat(doc.selectFirst("li").text()).isEqualTo("TEST")

        val breadcrumbs = doc.select(".breadcrumb-item")
        assertThat(breadcrumbs.count()).isEqualTo(1)
        assertThat(breadcrumbs.first().text()).isEqualTo("Something went wrong")
        assertThat(breadcrumbs.first().child(0).`is`("span"))
                .withFailMessage("Expected breadcrumb with null url to be a span").isTrue()
    }
}