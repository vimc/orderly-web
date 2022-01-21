package org.vaccineimpact.orderlyweb.tests.database_tests

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Before
import org.junit.Test
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.Tables.ORDERLYWEB_WORKFLOW_RUN_REPORTS
import org.vaccineimpact.orderlyweb.db.repositories.OrderlyWebWorkflowRunReportRepository
import org.vaccineimpact.orderlyweb.errors.BadRequest
import org.vaccineimpact.orderlyweb.errors.UnknownObjectError
import org.vaccineimpact.orderlyweb.test_helpers.CleanDatabaseTests
import org.vaccineimpact.orderlyweb.test_helpers.insertWorkflow
import org.vaccineimpact.orderlyweb.test_helpers.insertWorkflowRunReport
import org.vaccineimpact.orderlyweb.tests.insertUser
import java.sql.Timestamp
import java.time.Instant

class WorkflowRunReportRepositoryTests : CleanDatabaseTests()
{
    @Before
    fun createWorkflow()
    {
        insertUser("test@email.com", "Test User")
        insertWorkflow("test@email.com", "test_wf_key", "test_wf_name", Timestamp.from(Instant.now()),
            mapOf("source" to "dev"), "main", "abc123")
        insertWorkflowRunReport("test_wf_key", "test_report_key", "test_report_name",
            mapOf("param1" to "value1"))
    }

    @Test
    fun `can check report is in workflow`()
    {
        insertWorkflow("test@email.com", "another_wf_key", "another_wf_name")

        val sut = OrderlyWebWorkflowRunReportRepository()

        sut.checkReportIsInWorkflow("test_report_key", "test_wf_key")

        assertThatThrownBy { sut.checkReportIsInWorkflow("test_report_key", "another_wf_key") }
            .isInstanceOf(BadRequest::class.java)
            .hasMessageContaining("Report with key test_report_key does not belong to workflow with key another_wf_key")
    }

    @Test
    fun `can update report run`()
    {
        val sut = OrderlyWebWorkflowRunReportRepository()
        sut.updateReportRun("test_report_key", "success", "report_version_1", listOf("log1", "log2"))

        JooqContext().use {
            val result = it.dsl.select(
                ORDERLYWEB_WORKFLOW_RUN_REPORTS.REPORT,
                ORDERLYWEB_WORKFLOW_RUN_REPORTS.STATUS,
                ORDERLYWEB_WORKFLOW_RUN_REPORTS.REPORT_VERSION,
                ORDERLYWEB_WORKFLOW_RUN_REPORTS.LOGS
            )
                .from(ORDERLYWEB_WORKFLOW_RUN_REPORTS)
                .where(ORDERLYWEB_WORKFLOW_RUN_REPORTS.KEY.eq("test_report_key"))
                .fetchOne()

            assertThat(result[ORDERLYWEB_WORKFLOW_RUN_REPORTS.REPORT]).isEqualTo("test_report_name")
            assertThat(result[ORDERLYWEB_WORKFLOW_RUN_REPORTS.STATUS]).isEqualTo("success")
            assertThat(result[ORDERLYWEB_WORKFLOW_RUN_REPORTS.REPORT_VERSION]).isEqualTo("report_version_1")
            assertThat(result[ORDERLYWEB_WORKFLOW_RUN_REPORTS.LOGS]).isEqualTo("log1\nlog2")
        }
    }

    @Test
    fun `does not update report version if status is not success`()
    {
        val sut = OrderlyWebWorkflowRunReportRepository()
        sut.updateReportRun("test_report_key", "running", "report_version_1", listOf("log1", "log2"))
        JooqContext().use {
            val result = it.dsl.select(
                ORDERLYWEB_WORKFLOW_RUN_REPORTS.STATUS,
                ORDERLYWEB_WORKFLOW_RUN_REPORTS.REPORT_VERSION,
                ORDERLYWEB_WORKFLOW_RUN_REPORTS.LOGS
            )
            .from(ORDERLYWEB_WORKFLOW_RUN_REPORTS)
            .where(ORDERLYWEB_WORKFLOW_RUN_REPORTS.KEY.eq("test_report_key"))
            .fetchOne()

            assertThat(result[ORDERLYWEB_WORKFLOW_RUN_REPORTS.REPORT_VERSION]).isNull()
            assertThat(result[ORDERLYWEB_WORKFLOW_RUN_REPORTS.STATUS]).isEqualTo("running")
            assertThat(result[ORDERLYWEB_WORKFLOW_RUN_REPORTS.LOGS]).isEqualTo("log1\nlog2")
        }
    }

    @Test
    fun `can get report run`()
    {
        val sut = OrderlyWebWorkflowRunReportRepository()
        sut.updateReportRun("test_report_key", "success", "report_version_1", listOf("log1", "log2"))

        val result = sut.getReportRun("test_report_key")
        assertThat(result.email).isEqualTo("test@email.com")
        assertThat(result.date).isNull()
        assertThat(result.gitBranch).isEqualTo("main")
        assertThat(result.gitCommit).isEqualTo("abc123")
        assertThat(result.instances).isEqualTo(mapOf("source" to "dev"))
        assertThat(result.logs).isEqualTo("log1\nlog2")
        assertThat(result.params).isEqualTo(mapOf("param1" to "value1"))
        assertThat(result.report).isEqualTo("test_report_name")
        assertThat(result.reportVersion).isEqualTo("report_version_1")
        assertThat(result.status).isEqualTo("success")
    }

    @Test
    fun `get report run throws exception if report is unknown`()
    {
        val sut = OrderlyWebWorkflowRunReportRepository()
        assertThatThrownBy{ sut.getReportRun("nonexistent_key") }
            .isInstanceOf(UnknownObjectError::class.java)
    }
}
