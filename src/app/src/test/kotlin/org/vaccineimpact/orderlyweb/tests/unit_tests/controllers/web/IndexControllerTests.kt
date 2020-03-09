package org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.web

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.web.IndexController
import org.vaccineimpact.orderlyweb.db.OrderlyClient
import org.vaccineimpact.orderlyweb.models.ReportVersion
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.PermissionSet
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests
import org.vaccineimpact.orderlyweb.viewmodels.DownloadableFileViewModel
import org.vaccineimpact.orderlyweb.viewmodels.PinnedReportViewModel
import org.vaccineimpact.orderlyweb.viewmodels.ReportRowViewModel
import java.time.Duration
import java.time.Instant

class IndexControllerTests : TeamcityTests()
{

    private val globalReaderContext = mock<ActionContext> {
        on { permissions } doReturn PermissionSet(setOf(ReifiedPermission("reports.read",
                Scope.Global())))
    }

    @Test
    fun `builds report rows`()
    {
        val someDate = Instant.parse("2019-05-23T12:31:00.613Z")

        val r1v1 = ReportVersion("r1", null, "v1", "v2", true, someDate,
                mapOf("author" to "author1", "requester" to "requester1"), mapOf("p1" to "v1", "p2" to "v2"), listOf("t1", "t2"))
        val r1v2 = r1v1.copy(
                id = "v2",
                displayName = "r1 display name",
                published = false,
                date = someDate.plus(Duration.ofDays(1)))

        val r2v1 = ReportVersion("r2", null, "r2v1", "r2v1", true, someDate.minus(Duration.ofDays(2)),
                mapOf("author" to "another author", "requester" to "another requester"), mapOf(), listOf())

        val fakeReports = listOf(r1v1, r1v2, r2v1)

        val mockOrderly = mock<OrderlyClient> {
            on { this.getAllReportVersions() } doReturn fakeReports
            on { this.getReportTags(listOf("r1", "r2")) } doReturn mapOf("r1" to listOf("report tag"))
        }
        val sut = IndexController(globalReaderContext, mockOrderly)

        val result = sut.index().reports.sortedBy { it.ttKey }

        val r1Expected = ReportRowViewModel(
                ttKey = 1,
                ttParent = 0,
                name = "r1",
                displayName = "r1 display name",
                date = null,
                latestVersion = "v2",
                id = "v2",
                published = null,
                numVersions = 2,
                customFields = mapOf("author" to null, "requester" to null),
                parameterValues = null,
                tags = listOf("report tag"))

        val r1v1Expected = r1Expected.copy(
                ttKey = 2,
                ttParent = 1,
                published = false,
                date = "Fri May 24 2019",
                customFields = mapOf("author" to "author1", "requester" to "requester1"),
                parameterValues = "p1=v1, p2=v2",
                tags = listOf("t1", "t2")
        )

        val r1v2Expected = r1v1Expected.copy(
                ttKey = 3,
                date = "Thu May 23 2019",
                id = "v1",
                published = true)

        val r2Expected =
                ReportRowViewModel(
                        ttKey = 4,
                        ttParent = 0,
                        name = "r2",
                        displayName = "r2",
                        date = null,
                        latestVersion = "r2v1",
                        id = "r2v1",
                        published = null,
                        numVersions = 1,
                        customFields = mapOf("author" to null, "requester" to null),
                        parameterValues = null,
                        tags = listOf())

        val r2v1Expected =
                r2Expected.copy(
                        ttKey = 5,
                        ttParent = 4,
                        date = "Tue May 21 2019",
                        published = true,
                        customFields = mapOf("author" to "another author", "requester" to "another requester"),
                        parameterValues = null,
                        tags = listOf())

        val expected = listOf(r1Expected, r1v1Expected, r1v2Expected, r2Expected, r2v1Expected)
        assertThat(result.count()).isEqualTo(5)
        (0..4).map {
            assertThat(result[it]).isEqualToComparingFieldByField(expected[it])
        }
    }

    @Test
    fun `builds pinned versions`()
    {
        val someDate = Instant.parse("2019-06-07T12:31:00.613Z")

        val r1v1 = ReportVersion("r1", null, "v1", "v2", true, someDate,
                mapOf("author" to "author1", "requester" to "requester1"), mapOf("p" to "v"), listOf())
        val r1v2 = r1v1.copy(
                name = "r2",
                id = "v2",
                displayName = "r2 display name",
                date = someDate.plus(Duration.ofDays(1)))

        val fakeReports = listOf(r1v1, r1v2)

        val mockOrderly = mock<OrderlyClient> {
            on { this.getGlobalPinnedReports() } doReturn fakeReports
        }
        val sut = IndexController(globalReaderContext, mockOrderly)

        val result = sut.index().pinnedReports

        val expected = listOf(
                PinnedReportViewModel("r1", "v1", "r1", "Fri Jun 07 2019",
                        DownloadableFileViewModel("r1-v1.zip", "http://localhost:8888/report/r1/version/v1/all/", null)),
                PinnedReportViewModel("r2", "v2", "r2 display name", "Sat Jun 08 2019",
                        DownloadableFileViewModel("r2-v2.zip", "http://localhost:8888/report/r2/version/v2/all/", null))
        )

        assertThat(result.count()).isEqualTo(2)
        (0..1).map {
            assertThat(result[it]).isEqualToComparingFieldByField(expected[it])
        }
    }

    @Test
    fun `showProjectDocs if user has document reading permission`()
    {
        val documentReaderContext = mock<ActionContext> {
            on { hasPermission(ReifiedPermission("documents.read", Scope.Global())) } doReturn true
        }

        val mockOrderly = mock<OrderlyClient> {
            on { this.getGlobalPinnedReports() } doReturn listOf<ReportVersion>()
        }

        val noPermsContext = mock<ActionContext> {
            on { hasPermission(ReifiedPermission("documents.read", Scope.Global())) } doReturn false
        }

        var sut = IndexController(documentReaderContext, mockOrderly)
        var result = sut.index()
        assertThat(result.showProjectDocs).isTrue()

        sut = IndexController(noPermsContext, mockOrderly)
        result = sut.index()
        assertThat(result.showProjectDocs).isFalse()
    }

}

