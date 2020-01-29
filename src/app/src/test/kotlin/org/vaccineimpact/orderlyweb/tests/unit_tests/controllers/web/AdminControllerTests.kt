package org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.web

import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.orderlyweb.controllers.web.AdminController
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests
import org.vaccineimpact.orderlyweb.viewmodels.Breadcrumb
import org.vaccineimpact.orderlyweb.viewmodels.IndexViewModel

class AdminControllerTests : TeamcityTests()
{
    @Test
    fun `returns correct breadcrumbs for admin page`()
    {
        val sut = AdminController(mock(), mock())
        val model = sut.admin()
        assertThat(model.breadcrumbs).containsExactly(IndexViewModel.breadcrumb,
                Breadcrumb("Admin", "http://localhost:8888/admin"))
    }

}