package org.vaccineimpact.orderlyweb.tests.unit_tests.templates

import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.ClassRule
import org.junit.Test
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests
import org.vaccineimpact.orderlyweb.tests.unit_tests.templates.rules.FreemarkerTestRule
import org.vaccineimpact.orderlyweb.viewmodels.AdminViewModel

class AdminTests : TeamcityTests()
{
    companion object
    {
        @ClassRule
        @JvmField
        val template = FreemarkerTestRule("admin.ftl")
    }

    private val testViewModel = AdminViewModel(mock<ActionContext>())

    @Test
    fun `renders correctly`()
    {
        val doc = AdminTests.template.jsoupDocFor(testViewModel)
        assertThat(doc.select("#adminVueApp manage-roles")).isNotNull()
        assertThat(doc.select("#adminVueApp manage-users")).isNotNull()
        assertThat(doc.select("#adminVueApp manage-role-permissions")).isNotNull()
    }
}