package org.vaccineimpact.orderlyweb.tests.database_tests

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.Tables
import org.vaccineimpact.orderlyweb.db.repositories.OrderlyWebReportRunRepository
import org.vaccineimpact.orderlyweb.test_helpers.CleanDatabaseTests
import org.vaccineimpact.orderlyweb.tests.insertUser
import org.vaccineimpact.orderlyweb.tests.insertReportRun
import java.time.Instant


class ReportRunRepositoryTests : CleanDatabaseTests()
{
    @Test
    fun `can add report run`()
    {
        insertUser("user@email.com", "user.name")

        val now = Instant.now()

        val sut = OrderlyWebReportRunRepository()
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
    fun `can get all running reports for current user only`()
    {
         insertUser("user@email.com", "user.name")
         insertUser("user2@email.com", "user2.name")

        val now = Instant.now()

        insertReportRun(
            "adventurous_aardvark",
            "user@email.com",
            now,
            "report1",
            mapOf("instance1" to "pre-staging"),
            mapOf("parameter1" to "value1"),
            "branch1",
            "commit1"
        )

        insertReportRun(
            "benevolent_badger",
            "user2@email.com",
            now,
            "report2",
            mapOf("instance2" to "post-staging"),
            mapOf("parameter2" to "value2"),
            "branch1",
            "commit2"
        )

        val sut = OrderlyWebReportRunRepository()

        val result = sut.getAllRunningReports("user@email.com")
        assertThat(result.count()).isEqualTo(1)
        assertThat(result[0].date).isEqualTo(now)
        assertThat(result[0].name).isEqualTo("report1")
        assertThat(result[0].key).isEqualTo("adventurous_aardvark")
    }
}
