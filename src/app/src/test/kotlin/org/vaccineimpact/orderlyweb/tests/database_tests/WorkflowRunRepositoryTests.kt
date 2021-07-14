package org.vaccineimpact.orderlyweb.tests.database_tests

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.Tables
import org.vaccineimpact.orderlyweb.db.repositories.OrderlyWebWorkflowRunRepository
import org.vaccineimpact.orderlyweb.errors.UnknownObjectError
import org.vaccineimpact.orderlyweb.models.WorkflowReportWithParams
import org.vaccineimpact.orderlyweb.models.WorkflowRun
import org.vaccineimpact.orderlyweb.models.WorkflowRunReport
import org.vaccineimpact.orderlyweb.models.WorkflowRunSummary
import org.vaccineimpact.orderlyweb.test_helpers.CleanDatabaseTests
import org.vaccineimpact.orderlyweb.tests.insertUser
import java.time.Instant

class WorkflowRunRepositoryTests : CleanDatabaseTests()
{
    @Test
    fun `can add workflow run`()
    {
        insertUser("user@email.com", "user.name")

        val now = Instant.now()

        val sut = OrderlyWebWorkflowRunRepository()
        sut.addWorkflowRun(
            WorkflowRun(
                "Interim report",
                "adventurous_aardvark",
                "user@email.com",
                now,
                listOf(
                    WorkflowReportWithParams("reportA", mapOf("param1" to "one", "param2" to "two")),
                    WorkflowReportWithParams("reportB", mapOf("param3" to "three"))
                ),
                mapOf("instanceA" to "pre-staging"),
                "branch1",
                "commit1"
            )
        )
        sut.addWorkflowRun(
            WorkflowRun(
                "Final report",
                "benevolent_badger",
                "user@email.com",
                now,
                listOf(
                    WorkflowReportWithParams("reportC", mapOf("param4" to "four"))
                ),
                emptyMap()
            )
        )
        JooqContext().use {
            val result = it.dsl.selectFrom(Tables.ORDERLYWEB_WORKFLOW_RUN).fetch()

            assertThat(result.count()).isEqualTo(2)

            assertThat(result[0][Tables.ORDERLYWEB_WORKFLOW_RUN.ID]).isEqualTo(1)
            assertThat(result[0][Tables.ORDERLYWEB_WORKFLOW_RUN.NAME]).isEqualTo("Interim report")
            assertThat(result[0][Tables.ORDERLYWEB_WORKFLOW_RUN.KEY]).isEqualTo("adventurous_aardvark")
            assertThat(result[0][Tables.ORDERLYWEB_WORKFLOW_RUN.EMAIL]).isEqualTo("user@email.com")
            assertThat(result[0][Tables.ORDERLYWEB_WORKFLOW_RUN.DATE].toInstant()).isEqualTo(now)
            assertThat(result[0][Tables.ORDERLYWEB_WORKFLOW_RUN.REPORTS]).isEqualTo("""[{"name":"reportA","params":{"param1":"one","param2":"two"}},{"name":"reportB","params":{"param3":"three"}}]""")
            assertThat(result[0][Tables.ORDERLYWEB_WORKFLOW_RUN.INSTANCES]).isEqualTo("""{"instanceA":"pre-staging"}""")
            assertThat(result[0][Tables.ORDERLYWEB_WORKFLOW_RUN.GIT_BRANCH]).isEqualTo("branch1")
            assertThat(result[0][Tables.ORDERLYWEB_WORKFLOW_RUN.GIT_COMMIT]).isEqualTo("commit1")

            assertThat(result[1][Tables.ORDERLYWEB_WORKFLOW_RUN.ID]).isEqualTo(2)
            assertThat(result[1][Tables.ORDERLYWEB_WORKFLOW_RUN.NAME]).isEqualTo("Final report")
            assertThat(result[1][Tables.ORDERLYWEB_WORKFLOW_RUN.KEY]).isEqualTo("benevolent_badger")
            assertThat(result[1][Tables.ORDERLYWEB_WORKFLOW_RUN.EMAIL]).isEqualTo("user@email.com")
            assertThat(result[1][Tables.ORDERLYWEB_WORKFLOW_RUN.DATE].toInstant()).isEqualTo(now)
            val reports = Gson().fromJson<List<WorkflowReportWithParams>>(
                result[1][Tables.ORDERLYWEB_WORKFLOW_RUN.REPORTS],
                TypeToken.getParameterized(List::class.java, WorkflowReportWithParams::class.java).type
            )
            assertThat(reports[0].name).isEqualTo("reportC")
            assertThat(reports[0].params).isEqualTo(mapOf("param4" to "four"))
            assertThat(result[1][Tables.ORDERLYWEB_WORKFLOW_RUN.INSTANCES]).isEqualTo(Gson().toJson(emptyMap<String, String>()))
            assertThat(result[1][Tables.ORDERLYWEB_WORKFLOW_RUN.GIT_BRANCH]).isNull()
            assertThat(result[1][Tables.ORDERLYWEB_WORKFLOW_RUN.GIT_COMMIT]).isNull()
        }
    }

    @Test
    fun `cannot add workflow for non-existent user`()
    {
        val sut = OrderlyWebWorkflowRunRepository()
        assertThatThrownBy {
            sut.addWorkflowRun(
                WorkflowRun(
                    "Interim report",
                    "adventurous_aardvark",
                    "user@email.com",
                    Instant.now(),
                    emptyList(),
                    emptyMap()
                )
            )
        }.hasMessageContaining("FOREIGN KEY constraint failed")
    }

    @Test
    fun `cannot add workflow with duplicate key`()
    {
        insertUser("user@email.com", "user.name")

        val sut = OrderlyWebWorkflowRunRepository()
        sut.addWorkflowRun(
            WorkflowRun(
                "Interim report",
                "adventurous_aardvark",
                "user@email.com",
                Instant.now(),
                emptyList(),
                emptyMap()
            )
        )
        assertThatThrownBy {
            sut.addWorkflowRun(
                WorkflowRun(
                    "Interim report",
                    "adventurous_aardvark",
                    "user@email.com",
                    Instant.now(),
                    emptyList(),
                    emptyMap()
                )
            )
        }.hasMessageContaining("UNIQUE constraint failed: orderlyweb_workflow_run.key")
    }

    @Test
    fun `cannot add workflow with duplicate name and timestamp`()
    {
        insertUser("user@email.com", "user.name")

        val now = Instant.now()

        val sut = OrderlyWebWorkflowRunRepository()
        sut.addWorkflowRun(
            WorkflowRun(
                "Interim report",
                "adventurous_aardvark",
                "user@email.com",
                now,
                emptyList(),
                emptyMap()
            )
        )
        assertThatThrownBy {
            sut.addWorkflowRun(
                WorkflowRun(
                    "Interim report",
                    "benevolent_badger",
                    "user@email.com",
                    now,
                    emptyList(),
                    emptyMap()
                )
            )
        }.hasMessageContaining("UNIQUE constraint failed: orderlyweb_workflow_run.name, orderlyweb_workflow_run.date")
    }

    @Test
    fun `can get all workflow runs`()
    {
        insertUser("user@email.com", "user.name")

        val now = Instant.now()

        val sut = OrderlyWebWorkflowRunRepository()
        sut.addWorkflowRun(
            WorkflowRun(
                "Interim report",
                "adventurous_aardvark",
                "user@email.com",
                now,
                emptyList(),
                emptyMap()
            )
        )

        val result = sut.getWorkflowRunSummaries()
        assertThat(result.count()).isEqualTo(1)
        assertThat(result.first().name).isEqualTo("Interim report")
        assertThat(result.first().key).isEqualTo("adventurous_aardvark")
        assertThat(result.first().email).isEqualTo("user@email.com")
        assertThat(result.first().date).isEqualTo(now)
    }

    @Test
    fun `workflow runs are ordered reverse-chronologically`()
    {
        insertUser("user@email.com", "user.name")

        val sut = OrderlyWebWorkflowRunRepository()
        sut.addWorkflowRun(
            WorkflowRun(
                "Report two",
                "adventurous_aardvark",
                "user@email.com",
                Instant.now().minusMillis(1000),
                emptyList(),
                emptyMap()
            )
        )
        sut.addWorkflowRun(
            WorkflowRun(
                "Report one",
                "benevolent_badger",
                "user@email.com",
                Instant.now().minusMillis(2000),
                emptyList(),
                emptyMap()
            )
        )
        sut.addWorkflowRun(
            WorkflowRun(
                "Report three",
                "charming_chipmunk",
                "user@email.com",
                Instant.now(),
                emptyList(),
                emptyMap()
            )
        )

        val result = sut.getWorkflowRunSummaries()
        assertThat(result.count()).isEqualTo(3)
        assertThat(result[0].name).isEqualTo("Report three")
        assertThat(result[1].name).isEqualTo("Report two")
        assertThat(result[2].name).isEqualTo("Report one")
    }

    @Test
    fun `can get all workflow runs for user`()
    {
        insertUser("user@email.com", "user.name")
        insertUser("user2@email.com", "user2.name")

        val now = Instant.now()

        val sut = OrderlyWebWorkflowRunRepository()
        sut.addWorkflowRun(
            WorkflowRun(
                "Interim report",
                "adventurous_aardvark",
                "user@email.com",
                now,
                emptyList(),
                emptyMap()
            )
        )
        sut.addWorkflowRun(
            WorkflowRun(
                "Final report",
                "benevolent_badger",
                "user2@email.com",
                now,
                emptyList(),
                emptyMap()
            )
        )

        val result = sut.getWorkflowRunSummaries("user@email.com")
        assertThat(result.count()).isEqualTo(1)
        assertThat(result).isEqualTo(
            listOf(
                WorkflowRunSummary(
                    "Interim report",
                    "adventurous_aardvark",
                    "user@email.com",
                    now
                )
            )
        )
    }

    @Test
    fun `can get all workflow runs for user using empty name prefix`()
    {
        insertUser("user@email.com", "user.name")
        insertUser("user2@email.com", "user2.name")

        val now = Instant.now()

        val sut = OrderlyWebWorkflowRunRepository()
        sut.addWorkflowRun(
            WorkflowRun(
                "Interim report",
                "adventurous_aardvark",
                "user@email.com",
                now,
                emptyList(),
                emptyMap()
            )
        )
        sut.addWorkflowRun(
            WorkflowRun(
                "Final report",
                "benevolent_badger",
                "user2@email.com",
                now,
                emptyList(),
                emptyMap()
            )
        )

        val result = sut.getWorkflowRunSummaries("user2@email.com", "")
        assertThat(result.count()).isEqualTo(1)
        assertThat(result).isEqualTo(
            listOf(
                WorkflowRunSummary(
                    "Final report",
                    "benevolent_badger",
                    "user2@email.com",
                    now
                )
            )
        )
    }

    @Test
    fun `can get all workflow runs using non-empty name prefix`()
    {
        insertUser("user@email.com", "user.name")

        val now = Instant.now()

        val sut = OrderlyWebWorkflowRunRepository()
        sut.addWorkflowRun(
            WorkflowRun(
                "Interim report",
                "adventurous_aardvark",
                "user@email.com",
                now,
                emptyList(),
                emptyMap()
            )
        )
        sut.addWorkflowRun(
            WorkflowRun(
                "Final report",
                "benevolent_badger",
                "user@email.com",
                now,
                emptyList(),
                emptyMap()
            )
        )

        val result = sut.getWorkflowRunSummaries(namePrefix = "final")
        assertThat(result.count()).isEqualTo(1)
        assertThat(result).isEqualTo(
            listOf(
                WorkflowRunSummary(
                    "Final report",
                    "benevolent_badger",
                    "user@email.com",
                    now
                )
            )
        )
    }

    @Test
    fun `can get all workflow runs for user and non-empty name prefix`()
    {
        insertUser("user@email.com", "user.name")

        val now = Instant.now()

        val sut = OrderlyWebWorkflowRunRepository()
        sut.addWorkflowRun(
            WorkflowRun(
                "Interim report",
                "adventurous_aardvark",
                "user@email.com",
                now,
                emptyList(),
                emptyMap()
            )
        )
        sut.addWorkflowRun(
            WorkflowRun(
                "Final report",
                "benevolent_badger",
                "user@email.com",
                now,
                emptyList(),
                emptyMap()
            )
        )

        val result = sut.getWorkflowRunSummaries("user@email.com", "interim")
        assertThat(result.count()).isEqualTo(1)
        assertThat(result).isEqualTo(
            listOf(
                WorkflowRunSummary(
                    "Interim report",
                    "adventurous_aardvark",
                    "user@email.com",
                    now
                )
            )
        )
    }

    @Test
    fun `can get workflow details`()
    {
        insertUser("user@email.com", "user.name")

        val now = Instant.now()

        val sut = OrderlyWebWorkflowRunRepository()

        sut.addWorkflowRun(WorkflowRun(
                "Interim report",
                "adventurous_aardvark",
                "user@email.com",
                now,
                listOf(
                        WorkflowReportWithParams("reportA", mapOf("param1" to "one", "param2" to "two")),
                        WorkflowReportWithParams("reportB", mapOf("param3" to "three"))
                ),
                mapOf("instanceA" to "pre-staging"),
                "branch1",
                "commit1"
        ))

        val results = sut.getWorkflowRunDetails("adventurous_aardvark")
        assertThat(results).isEqualTo(WorkflowRun(
                "Interim report",
                "adventurous_aardvark",
                "user@email.com",
                now,
                listOf(
                        WorkflowReportWithParams("reportA", mapOf("param1" to "one", "param2" to "two")),
                        WorkflowReportWithParams("reportB", mapOf("param3" to "three"))
                ),
                mapOf("instanceA" to "pre-staging"),
                "branch1",
                "commit1"
        ))
    }

    @Test
    fun `does not get workflow details if key is invalid`()
    {
        insertUser("user@email.com", "user.name")

        val now = Instant.now()

        val sut = OrderlyWebWorkflowRunRepository()

        val workflowRun = WorkflowRun(
                "Interim report",
                "adventurous_aardvark",
                "user@email.com",
                now,
                listOf(
                        WorkflowReportWithParams("reportA", mapOf("param1" to "one", "param2" to "two")),
                        WorkflowReportWithParams("reportB", mapOf("param3" to "three"))
                ),
                mapOf("instanceA" to "pre-staging"),
                "branch1",
                "commit1"
        )
        sut.addWorkflowRun(workflowRun)
        assertThatThrownBy {
            sut.getWorkflowRunDetails("fake_key")
        }.hasMessageContaining("Unknown workflow : 'fake_key'")
    }

    @Test
    fun `can update status of a workflow`()
    {
        insertUser("user@email.com", "user.name")

        val sut = OrderlyWebWorkflowRunRepository()

        val workflowRun = WorkflowRun(
            "Interim report",
            "adventurous_aardvark",
            "user@email.com",
            Instant.now(),
            listOf(
                WorkflowReportWithParams("reportA", emptyMap())
            ),
            emptyMap()
        )

        sut.addWorkflowRun(workflowRun)

        JooqContext().use {
            val result = it.dsl.selectFrom(Tables.ORDERLYWEB_WORKFLOW_RUN).fetch()
            assertThat(result.count()).isEqualTo(1)
            assertThat(result[0][Tables.ORDERLYWEB_WORKFLOW_RUN.STATUS]).isNull()
        }

        sut.updateWorkflowRun(workflowRun.key, "success")

        JooqContext().use {
            val result = it.dsl.selectFrom(Tables.ORDERLYWEB_WORKFLOW_RUN).fetch()
            assertThat(result.count()).isEqualTo(1)
            assertThat(result[0][Tables.ORDERLYWEB_WORKFLOW_RUN.STATUS]).isEqualTo("success")
        }
    }

    @Test
    fun `cannot update status of an unknown workflow`()
    {
        insertUser("user@email.com", "user.name")

        val sut = OrderlyWebWorkflowRunRepository()

        assertThatThrownBy {
            sut.updateWorkflowRun("adventurous_aardvark", "success")
        }
            .isInstanceOf(UnknownObjectError::class.java)
            .hasMessageContaining("adventurous_aardvark")
    }

    @Test
    fun `cannot add workflow run report with duplicate report key`()
    {
        insertUser("user@email.com", "user.name")

        val sut = OrderlyWebWorkflowRunRepository()

        sut.addWorkflowRun(WorkflowRun(
                "Interim report",
                "fake_workflow_key",
                "user@email.com",
                Instant.now(),
                emptyList(),
                emptyMap()
        ))

        sut.addWorkflowRunReport(
                WorkflowRunReport(
                        "fake_workflow_key",
                        "adventurous_aardvark",
                        "report one",
                        emptyMap()
                )
        )
        assertThatThrownBy {
            sut.addWorkflowRunReport(
                    WorkflowRunReport(
                            "fake_workflow_key",
                            "adventurous_aardvark",
                            "report one",
                            emptyMap()
                    )
            )
        }.hasMessageContaining("UNIQUE constraint failed: orderlyweb_workflow_run_reports.report_key")
    }

    @Test
    fun `can get workflow run reports by report key`()
    {
        insertUser("user@email.com", "user.name")

        val sut = OrderlyWebWorkflowRunRepository()

        sut.addWorkflowRun(WorkflowRun(
                "Interim report",
                "fake_workflow_key",
                "user@email.com",
                Instant.now(),
                emptyList(),
                emptyMap()
        ))

        sut.addWorkflowRunReport(WorkflowRunReport(
                "fake_workflow_key",
                "adventurous_aardvark",
                "report one",
                mapOf("param1" to "one", "param2" to "two")
        ))

        val results = sut.getWorkflowRunReportByReportKey("adventurous_aardvark")

        assertThat(results).isEqualTo(WorkflowRunReport(
                "fake_workflow_key",
                "adventurous_aardvark",
                "report one",
                mapOf("param1" to "one", "param2" to "two")
        ))
    }

    @Test
    fun `can get workflow run reports by workflow key`()
    {
        insertUser("user@email.com", "user.name")

        val sut = OrderlyWebWorkflowRunRepository()

        sut.addWorkflowRun(WorkflowRun(
                "Interim report",
                "fake_workflow_key",
                "user@email.com",
                Instant.now(),
                emptyList(),
                emptyMap()
        ))

        sut.addWorkflowRunReport(WorkflowRunReport(
                "fake_workflow_key",
                "adventurous_aardvark",
                "report one",
                mapOf("param1" to "one", "param2" to "two")
        ))

        sut.addWorkflowRunReport(WorkflowRunReport(
                "fake_workflow_key",
                "adventurous_crook",
                "report two",
                mapOf("param1" to "one", "param2" to "two")
        ))

        val results = sut.getWorkflowRunReportByWorkflowKey("fake_workflow_key")

        assertThat(results).isEqualTo(listOf(WorkflowRunReport(
                "fake_workflow_key",
                "adventurous_aardvark",
                "report one",
                mapOf("param1" to "one", "param2" to "two")),

                WorkflowRunReport(
                        "fake_workflow_key",
                        "adventurous_crook",
                        "report two",
                        mapOf("param1" to "one", "param2" to "two"))
        ))
    }

    @Test
    fun `cannot get workflow run reports when workflow key does not exist `()
    {
        insertUser("user@email.com", "user.name")

        val sut = OrderlyWebWorkflowRunRepository()

        val results = sut.getWorkflowRunReportByWorkflowKey("fake_workflow_key")

        assertThat(results).isEmpty()
    }

    @Test
    fun `cannot get workflow run report when report key does not exist`()
    {
        insertUser("user@email.com", "user.name")

        val sut = OrderlyWebWorkflowRunRepository()

        assertThatThrownBy {
            sut.getWorkflowRunReportByReportKey("fake_workflow_key")
        }.hasMessageContaining("Unknown workflow : 'fake_workflow_key'")
    }
}