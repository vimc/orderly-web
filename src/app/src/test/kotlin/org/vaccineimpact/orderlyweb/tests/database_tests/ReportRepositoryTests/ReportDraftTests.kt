package org.vaccineimpact.orderlyweb.tests.database_tests.ReportRepositoryTests

import org.assertj.core.api.Assertions.*
import org.junit.Test
import org.vaccineimpact.orderlyweb.db.repositories.OrderlyReportRepository
import org.vaccineimpact.orderlyweb.test_helpers.CleanDatabaseTests
import org.vaccineimpact.orderlyweb.test_helpers.insertReport
import org.vaccineimpact.orderlyweb.test_helpers.insertVersionParameterValues
import org.vaccineimpact.orderlyweb.tests.InsertableChangelog
import org.vaccineimpact.orderlyweb.tests.insertChangelog

class ReportDraftTests: CleanDatabaseTests()
{
    @Test
    fun `reader cannot get drafts`() {
        insertReport("test", "v1", published = true)
        insertReport("test", "v2", published = false)
        insertReport("test", "v3", published = false)

        insertReport("test2", "v4", published = false)
        insertReport("test2", "v5", published = true)

        val sut = OrderlyReportRepository(isReviewer = false, isGlobalReader = true, reportReadingScopes = listOf())
        val result = sut.getUnpublishedVersions()

        assertThat(result.count()).isEqualTo(0)
    }

    @Test
    fun `reviewer can get all unpublished versions`()
    {
        insertReport("test", "v1", published = true)
        insertReport("test", "v2", published = false)
        insertReport("test", "v3", published = false)

        insertReport("test2", "v4", published = false)
        insertReport("test2", "v5", published = true)

        val sut = OrderlyReportRepository(true, true, listOf())
        val result = sut.getUnpublishedVersions()

        assertThat(result.count()).isEqualTo(3)
        assertThat(result[0].name).isEqualTo("test")
        assertThat(result[0].id).isEqualTo("v2")

        assertThat(result[1].name).isEqualTo("test")
        assertThat(result[1].id).isEqualTo("v3")

        assertThat(result[2].name).isEqualTo("test2")
        assertThat(result[2].id).isEqualTo("v4")
    }

    @Test
    fun `drafts include changelogs where they exist`()
    {
        insertReport("test", "v1", published = false)
        insertReport("test", "v2", published = false)
        insertReport("test2", "v3", published = false)

        insertChangelog(InsertableChangelog(
                "id1",
                "v1",
                "public",
                "did something great",
                0),
                InsertableChangelog(
                        "id2",
                        "v1",
                        "internal",
                        "did something internal",
                        1))

        val sut = OrderlyReportRepository(true, true, listOf())
        val result = sut.getUnpublishedVersions()

        assertThat(result.count()).isEqualTo(3)
        assertThat(result[0].id).isEqualTo("v1")
        assertThat(result[0].changelogs.count()).isEqualTo(2)
        assertThat(result[0].changelogs[0].value).isEqualTo("did something internal")
        assertThat(result[0].changelogs[0].label).isEqualTo("internal")
        assertThat(result[0].changelogs[1].value).isEqualTo("did something great")
        assertThat(result[0].changelogs[1].label).isEqualTo("public")

        assertThat(result[1].id).isEqualTo("v2")
        assertThat(result[1].changelogs.count()).isEqualTo(0)

        assertThat(result[2].id).isEqualTo("v3")
        assertThat(result[2].changelogs.count()).isEqualTo(0)
    }

    @Test
    fun `drafts include parameters where they exist`()
    {
        insertReport("test", "v1", published = false)
        insertReport("test", "v2", published = false)
        insertReport("test2", "v3", published = false)

        insertVersionParameterValues("v1", mapOf("p1" to "param1"))

        val sut = OrderlyReportRepository(true, true, listOf())
        val result = sut.getUnpublishedVersions()

        assertThat(result.count()).isEqualTo(3)
        assertThat(result[0].id).isEqualTo("v1")
        assertThat(result[0].parameterValues.count()).isEqualTo(1)
        assertThat(result[0].parameterValues["p1"]).isEqualTo("param1")

        assertThat(result[1].id).isEqualTo("v2")
        assertThat(result[1].parameterValues.count()).isEqualTo(0)

        assertThat(result[2].id).isEqualTo("v3")
        assertThat(result[2].parameterValues.count()).isEqualTo(0)
    }
}