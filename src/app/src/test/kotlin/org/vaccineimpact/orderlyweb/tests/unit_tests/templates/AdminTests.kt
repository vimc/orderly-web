package org.vaccineimpact.orderlyweb.tests.unit_tests.templates

import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.viewmodels.AdminViewModel

class AdminTests : FreeMarkerTest("admin.ftl")
{
    private val testViewModel = AdminViewModel(mock<ActionContext>(), true)

    @Test
    fun `renders correctly`()
    {
        val doc = jsoupDocFor(testViewModel)
        assertThat(doc.select("#adminVueApp manage-roles")).isNotNull()
        assertThat(doc.select("#adminVueApp manage-users")).isNotNull()
        assertThat(doc.select("#adminVueApp manage-role-permissions")).isNotNull()
        assertThat(doc.select("body script")[4].toString()).contains("let canAllowGuest = true;")
    }
}
