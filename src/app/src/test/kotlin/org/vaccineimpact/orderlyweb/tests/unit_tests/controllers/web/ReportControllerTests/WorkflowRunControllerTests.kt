package org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.web.ReportControllerTests

import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.orderlyweb.controllers.web.WorkflowRunController
import org.vaccineimpact.orderlyweb.viewmodels.Breadcrumb
import org.vaccineimpact.orderlyweb.viewmodels.IndexViewModel

class WorkflowRunControllerTests
{
    @Test
    fun `can get getRunWorkflow breadcrumbs`() {

        val sut = WorkflowRunController(mock())
        val model = sut.getRunWorkflow()

        assertThat(model.breadcrumbs).containsExactly(IndexViewModel.breadcrumb,
                Breadcrumb("Run a workflow", "http://localhost:8888/run-workflow"))
    }
}