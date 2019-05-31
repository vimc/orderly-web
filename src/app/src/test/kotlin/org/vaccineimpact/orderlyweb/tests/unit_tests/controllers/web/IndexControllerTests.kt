package org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.web

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.web.IndexController
import org.vaccineimpact.orderlyweb.db.Orderly
import org.vaccineimpact.orderlyweb.db.OrderlyClient
import org.vaccineimpact.orderlyweb.models.ReportVersion
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests
import org.vaccineimpact.orderlyweb.viewmodels.ReportRowViewModel
import java.time.Duration
import java.time.Instant

class IndexControllerTests : TeamcityTests()
{
    @Test
    fun `initialises Orderly correctly when user is reviewer`()
    {
        val mockContext = mock<ActionContext> {
            on { this.hasPermission(ReifiedPermission("reports.review", Scope.Global())) } doReturn true
        }

        val sut = IndexController(mockContext)

        assertThat((sut.orderly as Orderly).isReviewer).isTrue()
    }

    @Test
    fun `initialises Orderly correctly when user is not reviewer`()
    {
        val mockContext = mock<ActionContext> {
            on { this.hasPermission(ReifiedPermission("reports.review", Scope.Global())) } doReturn false
        }

        val sut = IndexController(mockContext)

        assertThat((sut.orderly as Orderly).isReviewer).isFalse()
    }

    @Test
    fun `builds report rows`()
    {
        val someDate = Instant.parse("2019-05-23T12:31:00.613Z")

        val r1v1 = ReportVersion("r1", null, "v1", "v2", true, someDate, "author1", "requester1")
        val r1v2 = r1v1.copy(
                id = "v2",
                displayName = "r1 display name",
                published = false,
                date = someDate.plus(Duration.ofDays(1)))

        val r2v1 = ReportVersion("r2", null, "r2v1", "r2v1", true, someDate.minus(Duration.ofDays(2)),
                "another author", "another requester")

        val fakeReports = listOf(r1v1, r1v2, r2v1)

        val mockOrderly = mock<OrderlyClient> {
            on { this.getAllReportVersions() } doReturn fakeReports
        }
        val sut = IndexController(mock(), mockOrderly)

        val result = sut.index().reports.sortedBy { it.ttKey }

        val r1Expected = ReportRowViewModel(
                ttKey = 1,
                ttParent = 0,
                name = "r1",
                displayName = "r1 display name",
                date = null,
                author = "author1",
                requester = "requester1",
                latestVersion = "v2",
                id = "v2",
                published = null,
                numVersions = 2)

        val r1v1Expected = r1Expected.copy(
                ttKey = 2,
                ttParent = 1,
                published = false,
                date = "Fri May 24 2019"
        )

        val r1v2Expected = r1Expected.copy(
                ttKey = 3,
                ttParent = 1,
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
                        author = "another author",
                        requester = "another requester",
                        latestVersion = "r2v1",
                        id = "r2v1",
                        published = null,
                        numVersions = 1)

        val r2v1Expected =
                r2Expected.copy(
                        ttKey = 5,
                        ttParent = 4,
                        date = "Tue May 21 2019",
                        published = true)

        val expected = listOf(r1Expected, r1v1Expected, r1v2Expected, r2Expected, r2v1Expected)
        assertThat(result.count()).isEqualTo(5)
        (0..4).map {
            assertThat(result[it]).isEqualToComparingFieldByField(expected[it])
        }
    }

    @Test
    fun `parent rows get all distinct authors and requesters from all versions`()
    {
        val v1Date = Instant.parse("2019-05-23T12:31:00.613Z")

        // distinct case
        val v1 = ReportVersion("r1", null, "v1", "v2", true, v1Date, "author1", "requester1")
        val v2 = ReportVersion("r1", null, "v2", "v2", false, v1Date.plus(Duration.ofDays(1)), "author2", "requester2")

        var mockOrderly = mock<OrderlyClient> {
            on { this.getAllReportVersions() } doReturn listOf(v1, v2)
        }
        var sut = IndexController(mock(), mockOrderly)

        var result = sut.index().reports.sortedBy { it.ttKey }

        assertThat(result[0].author).isEqualTo("author1,author2")
        assertThat(result[0].requester).isEqualTo("requester1,requester2")

        // non-distinct case
        val v3 = v2.copy(author = "author1", requester = "requester1")
        mockOrderly = mock {
            on { this.getAllReportVersions() } doReturn listOf(v1, v3)
        }
        sut = IndexController(mock(), mockOrderly)

        result = sut.index().reports.sortedBy { it.ttKey }

        assertThat(result[0].author).isEqualTo("author1")
        assertThat(result[0].requester).isEqualTo("requester1")
    }

    @Test
    fun `isReviewer is true when report reviewing permission is present in the context`()
    {
        val mockContext = mock<ActionContext> {
            on { this.hasPermission(ReifiedPermission("reports.review", Scope.Global())) } doReturn true
        }

        val sut = IndexController(mockContext, mock())
        val result = sut.index()
        assertThat(result.isReviewer).isTrue()
    }

    @Test
    fun `isReviewer is false when report reviewing permission is not present in the context`()
    {
        val mockContext = mock<ActionContext> {
            on { this.hasPermission(ReifiedPermission("reports.review", Scope.Global())) } doReturn false
        }

        val sut = IndexController(mockContext, mock())
        val result = sut.index()
        assertThat(result.isReviewer).isFalse()
    }

}

