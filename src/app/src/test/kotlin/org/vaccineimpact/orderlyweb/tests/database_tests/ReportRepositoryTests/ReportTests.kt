package org.vaccineimpact.orderlyweb.tests.database_tests.ReportRepositoryTests

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.Tables.ORDERLYWEB_PINNED_REPORT_GLOBAL
import org.vaccineimpact.orderlyweb.db.repositories.OrderlyReportRepository
import org.vaccineimpact.orderlyweb.db.repositories.ReportRepository
import org.vaccineimpact.orderlyweb.models.ReportWithDate
import org.vaccineimpact.orderlyweb.test_helpers.*
import java.sql.Timestamp
import java.time.Instant

class ReportTests : CleanDatabaseTests()
{
    private fun createSut(isReviewer: Boolean = false): ReportRepository
    {
        return OrderlyReportRepository(isReviewer, true, listOf())
    }

    @Test
    fun `reader can get all published reports`()
    {
        insertReport("test", "va")
        insertReport("test", "vz", display = "display name test")
        insertReport("test2", "vc")
        insertReport("test2", "vb", display = "display name test 2")
        insertReport("test2", "vd", published = false)
        insertReport("test3", "test3version", published = false)

        val sut = createSut()

        val results = sut.getAllReports()

        assertThat(results.count()).isEqualTo(2)

        assertThat(results[0].name).isEqualTo("test")
        assertThat(results[0].displayName).isEqualTo("display name test")
        assertThat(results[0].latestVersion).isEqualTo("vz")

        assertThat(results[1].name).isEqualTo("test2")
        assertThat(results[1].displayName).isEqualTo("display name test 2")
        assertThat(results[1].latestVersion).isEqualTo("vb")
    }

    @Test
    fun `getAllReports returns report names user is authorized to see`()
    {
        insertReport("goodname", "va")
        insertReport("badname", "vb")

        val mockContext = mock<ActionContext> {
            on { it.reportReadingScopes } doReturn listOf("goodname")
        }

        val sut = OrderlyReportRepository(mockContext)

        val result = sut.getAllReports()
        assertThat(result).hasSize(1)
        assertThat(result[0].name).isEqualTo("goodname")
    }

    @Test
    fun `getAllReports returns all report names if user has global read permissions`()
    {
        insertReport("goodname", "va")
        insertReport("badname", "vb")

        val mockContext = mock<ActionContext> {
            on { it.isGlobalReader() } doReturn true
        }

        val sut = OrderlyReportRepository(mockContext)

        val results = sut.getAllReports()
        assertThat(results.count()).isEqualTo(2)
    }

    @Test
    fun `getGlobalPinnedReports returns report names user is authorized to see`()
    {
        insertReport("goodname", "va")
        insertReport("badname", "vb")
        insertGlobalPinnedReport("goodname", 0)
        insertGlobalPinnedReport("badname", 1)

        val mockContext = mock<ActionContext> {
            on { it.reportReadingScopes } doReturn listOf("goodname")
        }

        val sut = OrderlyReportRepository(mockContext)

        val result = sut.getGlobalPinnedReports()
        assertThat(result).hasSize(1)
        assertThat(result[0].name).isEqualTo("goodname")
    }

    @Test
    fun `getGlobalPinnedReports returns all report names if user has global read permissions`()
    {
        insertReport("goodname", "va")
        insertReport("anothername", "vb")
        insertGlobalPinnedReport("goodname", 0)
        insertGlobalPinnedReport("anothername", 1)

        val mockContext = mock<ActionContext> {
            on { it.isGlobalReader() } doReturn true
        }

        val sut = OrderlyReportRepository(mockContext)

        val results = sut.getGlobalPinnedReports()
        assertThat(results.count()).isEqualTo(2)
    }

    @Test
    fun `reader can get all published report versions for report`()
    {
        insertReport("test", "version1")
        insertReport("test", "version2")
        insertReport("test", "version3", published = false)

        val sut = createSut()

        val results = sut.getReportsByName("test")

        assertThat(results.count()).isEqualTo(2)
        assertThat(results[0]).isEqualTo("version1")
        assertThat(results[1]).isEqualTo("version2")

    }

    @Test
    fun `reviewer can get all published and unpublished reports`()
    {
        insertReport("test", "va")
        insertReport("test", "vz")
        insertReport("test2", "vc")
        insertReport("test2", "vb")
        insertReport("test2", "vd", published = false)
        insertReport("test3", "test3version", published = false)

        val sut = createSut(isReviewer = true)

        val results = sut.getAllReports()

        assertThat(results.count()).isEqualTo(3)
        assertThat(results[0].name).isEqualTo("test")
        assertThat(results[1].name).isEqualTo("test2")
        assertThat(results[1].latestVersion).isEqualTo("vd")
        assertThat(results[2].name).isEqualTo("test3")
    }

    @Test
    fun `reviewer can get all published and unpublished report versions for report`()
    {
        insertReport("test", "version1")
        insertReport("test", "version2")
        insertReport("test", "version3", published = false)

        val sut = createSut(isReviewer = true)

        val results = sut.getReportsByName("test")

        assertThat(results.count()).isEqualTo(3)
        assertThat(results[0]).isEqualTo("version1")
        assertThat(results[1]).isEqualTo("version2")
        assertThat(results[2]).isEqualTo("version3")

    }

    @Test
    fun `can set published status`()
    {
        insertReport("test", "version1", published = false)

        val sut = createSut(isReviewer = true)

        var result = sut.setPublishStatus("test", "version1")

        assertThat(sut.getReportVersion("test", "version1").published).isTrue
        assertThat(result).isTrue

        result = sut.setPublishStatus("test", "version1", false)
        assertThat(result).isFalse

        assertThat(sut.getReportVersion("test", "version1").published).isFalse

    }

    @Test
    fun `can set published status when OrderlyWeb_Report_version does not yet exist`()
    {
        insertReport("test", "version1", addOrderlyWebReportVersion = false)
        val sut = createSut(isReviewer = true)

        var result = sut.setPublishStatus("test", "version1")
        assertThat(result).isTrue
        assertThat(sut.getReportVersion("test", "version1").published).isTrue

        result = sut.setPublishStatus("test", "version1", false)
        assertThat(result).isFalse

        assertThat(sut.getReportVersion("test", "version1").published).isFalse
    }

    @Test
    fun `reader can get latest published versions of pinned reports`()
    {
        insertReport("test1", "20170103-143015-1234pub")
        insertReport("test1", "20180103-143015-1234pub")
        insertReport("test1", "20190103-143015-1234unpub", published = false)

        insertReport("test2", "20160203-143015-1234pub")

        insertReport("test3", "20170203-143015-1234pub")

        insertReport("test4", "20180203-143015-1234unpub", published = false)

        insertGlobalPinnedReport("test4", 0)
        insertGlobalPinnedReport("test3", 1)
        insertGlobalPinnedReport("test1", 2)

        val sut = createSut(isReviewer = false)

        val results = sut.getGlobalPinnedReports()

        assertThat(results.count()).isEqualTo(2)
        assertThat(results[0].name).isEqualTo("test3")
        assertThat(results[0].latestVersion).isEqualTo("20170203-143015-1234pub")
        assertThat(results[1].name).isEqualTo("test1")
        assertThat(results[1].latestVersion).isEqualTo("20180103-143015-1234pub")
    }

    @Test
    fun `reviewer gets latest published versions of pinned reports`()
    {
        insertReport("test1", "20170103-143015-1234pub")
        insertReport("test1", "20180103-143015-1234pub")
        insertReport("test1", "20190103-143015-1234unpub", published = false)

        insertReport("test2", "20160203-143015-1234pub")

        insertReport("test3", "20170203-143015-1234pub")

        insertReport("test4", "20180203-143015-1234unpub", published = false)

        insertGlobalPinnedReport("test4", 0)
        insertGlobalPinnedReport("test3", 1)
        insertGlobalPinnedReport("test1", 2)

        val sut = createSut(isReviewer = true)

        val results = sut.getGlobalPinnedReports()

        assertThat(results.count()).isEqualTo(2)
        assertThat(results[0].name).isEqualTo("test3")
        assertThat(results[0].latestVersion).isEqualTo("20170203-143015-1234pub")
        assertThat(results[1].name).isEqualTo("test1")
        assertThat(results[1].latestVersion).isEqualTo("20180103-143015-1234pub")
    }

    @Test
    fun `setPinnedReport deletes existing pinned reports and inserts from parameter`()
    {
        insertReport("r1", "v1")
        insertReport("r2", "v2")
        insertReport("r3", "v3")
        insertReport("r4", "v4")

        insertGlobalPinnedReport("r1", 0)
        insertGlobalPinnedReport("r2", 1)

        val sut = createSut()
        sut.setGlobalPinnedReports(listOf("r4", "r3"))

        JooqContext().use {
            val result = it.dsl.selectFrom(ORDERLYWEB_PINNED_REPORT_GLOBAL)
                    .orderBy(ORDERLYWEB_PINNED_REPORT_GLOBAL.ORDERING)
                    .fetch()

            assertThat(result.count()).isEqualTo(2)
            assertThat(result[0][ORDERLYWEB_PINNED_REPORT_GLOBAL.ORDERING]).isEqualTo(0)
            assertThat(result[0][ORDERLYWEB_PINNED_REPORT_GLOBAL.REPORT]).isEqualTo("r4")
            assertThat(result[1][ORDERLYWEB_PINNED_REPORT_GLOBAL.ORDERING]).isEqualTo(1)
            assertThat(result[1][ORDERLYWEB_PINNED_REPORT_GLOBAL.REPORT]).isEqualTo("r3")
        }
    }

    @Test
    fun `can check reportExists`()
    {
        val sut = createSut()
        assertThat(sut.reportExists("r1")).isFalse()

        insertReport("r1", "v1")
        assertThat(sut.reportExists("r1")).isTrue()
    }


    @Test
    fun `can get reports with publish status`()
    {
        insertReport("test", "v1", published = true, display = null)
        insertReport("test", "v2", published = false, display = "old display name")
        insertReport("test", "v3", published = false, display = "newer display name")

        insertReport("test2", "v4", published = true, display = "test2 display name")
        insertReport("test2", "v5", published = false, display = null)

        insertReport("test3", "v6", published = false, display = null)

        val sut = createSut()
        val result = sut.getReportsWithPublishStatus()
        assertThat(result.count()).isEqualTo(3)

        assertThat(result[0].name).isEqualTo("test3")
        assertThat(result[0].displayName).isEqualTo(null)
        assertThat(result[0].hasBeenPublished).isEqualTo(false)

        assertThat(result[1].name).isEqualTo("test2")
        assertThat(result[1].displayName).isEqualTo(null)
        assertThat(result[1].hasBeenPublished).isEqualTo(true)

        assertThat(result[2].name).isEqualTo("test")
        assertThat(result[2].displayName).isEqualTo("newer display name")
        assertThat(result[2].hasBeenPublished).isEqualTo(true)
    }

    @Test
    fun `can get latest dates for selected reports`()
    {
        val now = Instant.ofEpochMilli(1655378424228)

        insertReport("test1", "v1", date = Timestamp.from(now.minusSeconds(60)))
        insertReport("test1", "v2", date = Timestamp.from(now))
        insertReport("test1", "v3", date = Timestamp.from(now.minusSeconds(120)))
        insertReport("test2", "v4", date = Timestamp.from(now))

        val sut = createSut()
        val result = sut.getLatestReportVersions(
                listOf(
                        "test1",
                        "test2"
                )
        )
        assertThat(result).containsExactly(
                ReportWithDate("test1", now),
                ReportWithDate("test2", now)
        )
    }
}