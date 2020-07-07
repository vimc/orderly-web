package org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.web.ReportControllerTests

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.orderlyweb.controllers.web.ReportController
import org.vaccineimpact.orderlyweb.db.repositories.ReportRepository
import org.vaccineimpact.orderlyweb.models.Changelog
import org.vaccineimpact.orderlyweb.models.ReportVersionWithChangelogsParams
import org.vaccineimpact.orderlyweb.models.ReportWithPublishStatus
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests
import java.time.Instant
import java.time.temporal.ChronoUnit

class ReportDraftTests : TeamcityTests()
{
    @Test
    fun `can build report draft view models with most recent drafts first`()
    {
        val date = Instant.parse("2020-04-21T10:34:50.63Z")

        val fakeReports = listOf(
                ReportWithPublishStatus("report-2", "The second report", false),
                ReportWithPublishStatus("report-1", "The first report", true))
        val fakeDrafts = listOf(
                ReportVersionWithChangelogsParams("report-1", "The first report", "v1-1", date.minus(1, ChronoUnit.DAYS), false, mapOf(), listOf()),
                ReportVersionWithChangelogsParams("report-2", "report-2", "v2-1", date.minus(1, ChronoUnit.DAYS), false, mapOf(), listOf()),
                ReportVersionWithChangelogsParams("report-2", "The second report", "v2-2", date.minus(1, ChronoUnit.HOURS), false, mapOf(), listOf()),
                ReportVersionWithChangelogsParams("report-2", "The second report", "v2-3", date, false, mapOf(), listOf()))

        val mockRepo = mock<ReportRepository> {
            on { getReportsWithPublishStatus() } doReturn fakeReports
            on { getDrafts() } doReturn fakeDrafts
        }

        val sut = ReportController(mock(), mock(), mockRepo, mock())

        val result = sut.getReportDrafts()

        assertThat(result.count()).isEqualTo(2)
        assertThat(result[0].previouslyPublished).isFalse()
        assertThat(result[0].dateGroups.count()).isEqualTo(2)

        var dateGroup = result[0].dateGroups[0]
        assertThat(dateGroup.date).isEqualTo("Tue Apr 21 2020")
        assertThat(dateGroup.drafts.count()).isEqualTo(2)
        assertThat(dateGroup.drafts[0].id).isEqualTo("v2-3")
        assertThat(dateGroup.drafts[0].url).isEqualTo("http://localhost:8888/report-2/v2-3")
        assertThat(dateGroup.drafts[1].id).isEqualTo("v2-2")
        assertThat(dateGroup.drafts[1].url).isEqualTo("http://localhost:8888/report-2/v2-2")

        dateGroup = result[0].dateGroups[1]
        assertThat(dateGroup.date).isEqualTo("Mon Apr 20 2020")
        assertThat(dateGroup.drafts.count()).isEqualTo(1)
        assertThat(dateGroup.drafts[0].id).isEqualTo("v2-1")
        assertThat(dateGroup.drafts[0].url).isEqualTo("http://localhost:8888/report-2/v2-1")

        assertThat(result[1].previouslyPublished).isTrue()
        assertThat(result[1].dateGroups.count()).isEqualTo(1)
        dateGroup = result[1].dateGroups[0]
        assertThat(dateGroup.date).isEqualTo("Mon Apr 20 2020")
        assertThat(dateGroup.drafts.count()).isEqualTo(1)
        assertThat(dateGroup.drafts[0].id).isEqualTo("v1-1")
        assertThat(dateGroup.drafts[0].url).isEqualTo("http://localhost:8888/report-1/v1-1")
    }

    @Test
    fun `only reports with drafts are included`()
    {
        val date = Instant.parse("2020-04-21T10:34:50.63Z")

        val fakeReports = listOf(
                ReportWithPublishStatus("report-2", "The second report", false),
                ReportWithPublishStatus("report-1", "The first report", true))
        val fakeDrafts = listOf(
                ReportVersionWithChangelogsParams("report-1",
                        "The first report",
                        "v1-1",
                        date,
                        false,
                        mapOf("p1" to "param1", "p2" to "param2"),
                        listOf(Changelog("v1-1", "public", "something public", true, true),
                                Changelog("v1-1", "internal", "something internal", true, false))))

        val mockRepo = mock<ReportRepository> {
            on { getReportsWithPublishStatus() } doReturn fakeReports
            on { getDrafts() } doReturn fakeDrafts
        }

        val sut = ReportController(mock(), mock(), mockRepo, mock())
        val result = sut.getReportDrafts()
        assertThat(result.count()).isEqualTo(1)
    }

    @Test
    fun `can build report draft view model with parameters and changelogs`()
    {
        val date = Instant.parse("2020-04-21T10:34:50.63Z")
        val fakeReports = listOf(ReportWithPublishStatus("report-1", "The first report", false))
        val fakeDrafts = listOf(
                ReportVersionWithChangelogsParams("report-1",
                        "The first report",
                        "v1-1",
                        date,
                        false,
                        mapOf("p1" to "param1", "p2" to "param2"),
                        listOf(Changelog("v1-1", "published", "something public", true, true),
                                Changelog("v1-1", "draft", "something internal", true, false))))

        val mockRepo = mock<ReportRepository> {
            on { getReportsWithPublishStatus() } doReturn fakeReports
            on { getDrafts() } doReturn fakeDrafts
        }

        val sut = ReportController(mock(), mock(), mockRepo, mock())
        val result = sut.getReportDrafts()
        val draft = result[0].dateGroups[0].drafts[0]
        assertThat(draft.parameterValues).isEqualTo("p1=param1,p2=param2")
        assertThat(draft.changelog.count()).isEqualTo(2)
        assertThat(draft.changelog[0].cssClass).isEqualTo("public")
        assertThat(draft.changelog[0].label).isEqualTo("published")
        assertThat(draft.changelog[0].value).isEqualTo("something public")
        assertThat(draft.changelog[1].cssClass).isEqualTo("internal")
        assertThat(draft.changelog[1].label).isEqualTo("draft")
        assertThat(draft.changelog[1].value).isEqualTo("something internal")
    }

    @Test
    fun `report display name falls back to report name`()
    {
        val date = Instant.parse("2020-04-21T10:34:50.63Z")

        val fakeReports = listOf(
                ReportWithPublishStatus("report-2", "The second report", false),
                ReportWithPublishStatus("report-1", null, false))

        val fakeDrafts = listOf(
                ReportVersionWithChangelogsParams("report-1", "The first report", "v1-1", date, false, mapOf(), listOf()),
                ReportVersionWithChangelogsParams("report-2", null, "v2-1", date.minus(1, ChronoUnit.DAYS), false, mapOf(), listOf()))

        val mockRepo = mock<ReportRepository> {
            on { getReportsWithPublishStatus() } doReturn fakeReports
            on { getDrafts() } doReturn fakeDrafts
        }

        val sut = ReportController(mock(), mock(), mockRepo, mock())
        val result = sut.getReportDrafts()

        assertThat(result[0].displayName).isEqualTo("The second report")
        assertThat(result[1].displayName).isEqualTo("report-1")
    }
}