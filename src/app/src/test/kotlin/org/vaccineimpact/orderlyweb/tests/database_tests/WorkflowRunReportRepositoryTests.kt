package org.vaccineimpact.orderlyweb.tests.database_tests

import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.vaccineimpact.orderlyweb.db.repositories.OrderlyWebWorkflowRunReportRepository
import org.vaccineimpact.orderlyweb.errors.BadRequest
import org.vaccineimpact.orderlyweb.test_helpers.CleanDatabaseTests
import org.vaccineimpact.orderlyweb.test_helpers.insertWorkflow
import org.vaccineimpact.orderlyweb.test_helpers.insertWorkflowRunReport
import org.vaccineimpact.orderlyweb.tests.insertUser

class WorkflowRunReportRepositoryTests : CleanDatabaseTests()
{
    @Test
    fun `can check report is in workflow`()
    {
        insertUser("test@email.com", "Test User")
        insertWorkflow("test@email.com", "test_wf_key", "test_wf_name")
        insertWorkflowRunReport("test_wf_key", "test_report_key", "test_report_name", mapOf())
        insertWorkflow("test@email.com", "another_wf_key", "another_wf_name")

        val sut = OrderlyWebWorkflowRunReportRepository()

        sut.checkReportIsInWorkflow("test_report_key", "test_wf_key")

        assertThatThrownBy{ sut.checkReportIsInWorkflow("test_report_key", "another_wf_key") }
            .isInstanceOf(BadRequest::class.java)
            .hasMessageContaining("Report with key test_report_key does not belong to workflow with key another_wf_key")
    }
}
