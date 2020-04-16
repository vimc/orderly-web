package org.vaccineimpact.orderlyweb.tests.database_tests.ReportRepositoryTests

import org.assertj.core.api.Assertions.*
import org.junit.Test
import org.vaccineimpact.orderlyweb.db.repositories.OrderlyReportRepository
import org.vaccineimpact.orderlyweb.test_helpers.insertReport

class ReportDraftTests
{
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
        assertThat(result[1].id).isEqualTo("v2")

        assertThat(result[2].name).isEqualTo("test2")
        assertThat(result[2].id).isEqualTo("v4")
    }
}