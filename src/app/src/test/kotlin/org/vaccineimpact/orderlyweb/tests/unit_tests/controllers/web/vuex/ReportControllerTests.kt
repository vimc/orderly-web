package org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.web.vuex

import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.vaccineimpact.orderlyweb.controllers.web.vuex.ReportController
import org.vaccineimpact.orderlyweb.viewmodels.Breadcrumb
import org.vaccineimpact.orderlyweb.viewmodels.IndexViewModel

class ReportControllerTests
{
    @Test
    fun `can getRunReport breadcrumbs`()
    {
        val sut = ReportController(mock())
        val model = sut.getRunReport()

        assertThat(model.breadcrumbs).containsExactly(
                IndexViewModel.breadcrumb,
                Breadcrumb("Run a report", "http://localhost:8888/vuex-run-report")
        )
    }

}