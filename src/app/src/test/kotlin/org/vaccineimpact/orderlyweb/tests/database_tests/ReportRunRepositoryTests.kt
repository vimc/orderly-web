package org.vaccineimpact.orderlyweb.tests.database_tests

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.Tables
import org.vaccineimpact.orderlyweb.db.repositories.OrderlyWebReportRunRepository
import org.vaccineimpact.orderlyweb.models.ReportRunLog
import org.vaccineimpact.orderlyweb.test_helpers.CleanDatabaseTests
import org.vaccineimpact.orderlyweb.test_helpers.insertReport
import org.vaccineimpact.orderlyweb.tests.insertUser
import java.time.Instant
import org.vaccineimpact.orderlyweb.models.ReportRunWithDate

class ReportRunRepositoryTests : CleanDatabaseTests()
{
    private val now = Instant.now()

    private fun addTestReportRun(sut: OrderlyWebReportRunRepository)
    {
        sut.addReportRun(
                "adventurous_aardvark",
                "user@email.com",
                now,
                "report1",
                mapOf("instance1" to "pre-staging"),
                mapOf("parameter1" to "value1"),
                "branch1",
                "commit1"
        )
    }

    @Test
    fun `can add report run`()
    {
        insertUser("user@email.com", "user.name")

        val sut = OrderlyWebReportRunRepository()

        addTestReportRun(sut)
        sut.addReportRun(
            "benevolent_badger",
            "user@email.com",
            now,
            "report2",
            mapOf("instance2" to "post-staging"),
            mapOf("parameter2" to "value2"),
            "branch1",
            "commit2"
        )
        JooqContext().use {
            val result = it.dsl.selectFrom(Tables.ORDERLYWEB_REPORT_RUN).fetch()

            assertThat(result.count()).isEqualTo(2)

            assertThat(result[0][Tables.ORDERLYWEB_REPORT_RUN.ID]).isEqualTo(1)
            assertThat(result[0][Tables.ORDERLYWEB_REPORT_RUN.KEY]).isEqualTo("adventurous_aardvark")
            assertThat(result[0][Tables.ORDERLYWEB_REPORT_RUN.EMAIL]).isEqualTo("user@email.com")
            assertThat(result[0][Tables.ORDERLYWEB_REPORT_RUN.DATE].toInstant()).isEqualTo(now)
            assertThat(result[0][Tables.ORDERLYWEB_REPORT_RUN.REPORT]).isEqualTo("report1")
            assertThat(result[0][Tables.ORDERLYWEB_REPORT_RUN.INSTANCES]).isEqualTo("""{"instance1":"pre-staging"}""")
            assertThat(result[0][Tables.ORDERLYWEB_REPORT_RUN.PARAMS]).isEqualTo("""{"parameter1":"value1"}""")
            assertThat(result[0][Tables.ORDERLYWEB_REPORT_RUN.GIT_BRANCH]).isEqualTo("branch1")
            assertThat(result[0][Tables.ORDERLYWEB_REPORT_RUN.GIT_COMMIT]).isEqualTo("commit1")

            assertThat(result[1][Tables.ORDERLYWEB_REPORT_RUN.ID]).isEqualTo(2)
        }
    }

    @Test
    fun `cannot add report for non-existent user`()
    {
        val sut = OrderlyWebReportRunRepository()
        assertThatThrownBy {
            sut.addReportRun(
                "adventurous_aardvark",
                "test.user@example.com",
                Instant.now(),
                "report2",
                mapOf("instance2" to "post-staging"),
                mapOf("parameter2" to "value2"),
                "branch1",
                "commit2"
            )
        }.hasMessageContaining("FOREIGN KEY constraint failed")
    }

    @Test
    fun `can get running report logs for key`()
    {
        insertUser("user@email.com", "user.name")

        val sut = OrderlyWebReportRunRepository()
        addTestReportRun(sut)

        assertThat(sut.getReportRun("adventurous_aardvark")).isEqualTo(ReportRunLog(
                "user@email.com",
                now,
                "report1",
                mapOf("instance1" to "pre-staging"),
                mapOf("parameter1" to "value1"),
                "branch1",
                "commit1",
                null,
                null,
                null))
    }

    @Test
    fun `can throw UnknownObjectError when retrieving unknown running report logs`()
    {
        val sut = OrderlyWebReportRunRepository()
        assertThatThrownBy {
            sut.getReportRun("fakeKey")
        }.hasMessageContaining("the following problems occurred:\n" +
                "Unknown get-report-run : 'key'")
    }

    @Test
    fun `can update report run`()
    {
        insertUser("user@email.com", "user.name")
        insertReport("testReport", "version123")

        val sut = OrderlyWebReportRunRepository()
        addTestReportRun(sut)

        sut.updateReportRun(
                "adventurous_aardvark",
                "success",
                "version123",
                listOf("log1","log2")
        )

        assertThat(sut.getReportRun("adventurous_aardvark")).isEqualTo(ReportRunLog(
                "user@email.com",
                now,
                "report1",
                mapOf("instance1" to "pre-staging"),
                mapOf("parameter1" to "value1"),
                "branch1",
                "commit1",
                "success",
                "log1\nlog2",
                "version123"))
    }

    @Test
    fun `update report run does not update version if status is not 'success'`()
    {
        insertUser("user@email.com", "user.name")

        val sut = OrderlyWebReportRunRepository()
        addTestReportRun(sut)

        sut.updateReportRun(
                "adventurous_aardvark",
                "running",
                "version123",
                listOf("log1", "log2")
        )

        assertThat(sut.getReportRun("adventurous_aardvark")).isEqualTo(ReportRunLog(
                "user@email.com",
                now,
                "report1",
                mapOf("instance1" to "pre-staging"),
                mapOf("parameter1" to "value1"),
                "branch1",
                "commit1",
                "running",
                "log1\nlog2",
                null))
    }

    fun `can get all non-workflow running reports for current user only`()
    {
         insertUser("user@email.com", "user.name")
         insertUser("user2@email.com", "user2.name")

        val now = Instant.now()

        val sut = OrderlyWebReportRunRepository()

        sut.addReportRun(
            "adventurous_aardvark",
            "user@email.com",
            now,
            "report1",
            mapOf("instance1" to "post-staging"),
            mapOf("parameter1" to "value1"),
            "branch1",
            "commit1"
        )

        sut.addReportRun(
            "benevolent_badger",
            "user2@email.com",
            now,
            "report2",
            mapOf("instance2" to "post-staging"),
            mapOf("parameter2" to "value2"),
            "branch1",
            "commit2"
        )

        sut.addReportRun(
            "cantankerous_capybara",
            "user2@email.com",
            now,
            "report3",
            mapOf("instance3" to "post-staging"),
            mapOf("parameter3" to "value2"),
            "branch3",
            "commit3",
            "workflow1"
        )

        val result = sut.getAllReportRunsForUser("user@email.com")
        assertThat(result.count()).isEqualTo(1)
        assertThat(result)
                .isEqualTo(listOf(ReportRunWithDate("report1", "adventurous_aardvark", now)))
    }
}
