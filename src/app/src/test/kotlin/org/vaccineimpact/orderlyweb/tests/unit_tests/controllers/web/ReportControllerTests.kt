package org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.web

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.web.ReportController
import org.vaccineimpact.orderlyweb.db.Config
import org.vaccineimpact.orderlyweb.db.Orderly
import org.vaccineimpact.orderlyweb.db.OrderlyClient
import org.vaccineimpact.orderlyweb.models.*
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests
import org.vaccineimpact.orderlyweb.viewmodels.ChangelogItemViewModel
import java.time.Instant

class ReportControllerTests : TeamcityTests()
{
    private val versionId = "20170103-143015-1234abcd"
    private val mockReportDetails = ReportVersionDetails("r1",
            "a fake report",
            versionId,
            true,
            Instant.now(),
            "dr author",
            "ms funder",
            "a fake report",
            listOf(),
            listOf(),
            listOf())

    private val mockChangelog = listOf(Changelog("20160103-143015-1234abcd", "internal", "something internal", true),
            Changelog("20160103-143015-1234abcd", "public", "something public", true),
            Changelog("20170106-143015-1234abcd", "internal", "something internal in 2017", true),
            Changelog("20180103-143015-1234abcd", "public", "something public in 2018", true))

    private val mockActionContext = mock<ActionContext> {
        on { this.params(":name") } doReturn "r1"
        on { this.params(":version") } doReturn versionId
    }

    private val mockOrderly = mock<OrderlyClient> {

        on { this.getDetailsByNameAndVersion("r1", versionId) } doReturn mockReportDetails
        on { this.getReportsByName("r1") } doReturn
                listOf(versionId, "20170104-091500-1234dcba")
        on { this.getChangelogByNameAndVersion("r1", versionId) } doReturn mockChangelog
    }

    @Test
    fun `getByNameAndVersion uses display name if present`()
    {
        val sut = ReportController(mockActionContext, mockOrderly)
        val result = sut.getByNameAndVersion()

        assertThat(result.report).isEqualTo(mockReportDetails)
    }

    @Test
    fun `getByNameAndVersion uses name for display name if not present`()
    {
        val orderly = mock<OrderlyClient> {
            on { this.getDetailsByNameAndVersion("r1", versionId) } doReturn
                    mockReportDetails.copy(displayName = null)
        }

        val sut = ReportController(mockActionContext, orderly)
        val result = sut.getByNameAndVersion()

        assertThat(result.report.displayName).isEqualTo("r1")
    }

    @Test
    fun `builds report version picker viewmodels`()
    {
        val sut = ReportController(mockActionContext, mockOrderly)
        val result = sut.getByNameAndVersion()

        assertThat(result.versions[1].url).isEqualTo("http://localhost:8888/report/r1/20170103-143015-1234abcd")
        assertThat(result.versions[1].date).isEqualTo("Tue Jan 03 2017, 14:30")
        assertThat(result.versions[1].selected).isTrue()

        assertThat(result.versions[0].url).isEqualTo("http://localhost:8888/report/r1/20170104-091500-1234dcba")
        assertThat(result.versions[0].date).isEqualTo("Wed Jan 04 2017, 09:15")
        assertThat(result.versions[0].selected).isFalse()
    }

    @Test
    fun `report version picker options are ordered by date descending`()
    {
        val mockOrderly = mock<OrderlyClient> {

            on { this.getDetailsByNameAndVersion("r1", versionId) } doReturn mockReportDetails
            on { this.getReportsByName("r1") } doReturn
                    listOf("20170104-091500-1234dcba", "20170204-093000-1234dcba", versionId, "20170202-093000-1234dcba")
            on { this.getChangelogByNameAndVersion("r1", versionId) } doReturn mockChangelog
        }

        val sut = ReportController(mockActionContext, mockOrderly)
        val result = sut.getByNameAndVersion()

        assertThat(result.versions[0].date).isEqualTo("Sat Feb 04 2017, 09:30")
        assertThat(result.versions[1].date).isEqualTo("Thu Feb 02 2017, 09:30")
        assertThat(result.versions[2].date).isEqualTo("Wed Jan 04 2017, 09:15")
        assertThat(result.versions[3].date).isEqualTo("Tue Jan 03 2017, 14:30")
    }


    @Test
    fun `builds changelog viewmodels`()
    {
        val sut = ReportController(mockActionContext, mockOrderly)
        val result = sut.getByNameAndVersion()

        assertThat(result.changelog.count()).isEqualTo(3)
        assertThat(result.changelog[0].date).isEqualTo("Wed Jan 03 2018, 14:30")
        assertThat(result.changelog[0].version).isEqualTo("20180103-143015-1234abcd")
        var expectedEntries = listOf(ChangelogItemViewModel("public", "something public in 2018"))
        assertThat(result.changelog[0].entries).hasSameElementsAs(expectedEntries)

        assertThat(result.changelog[1].date).isEqualTo("Fri Jan 06 2017, 14:30")
        assertThat(result.changelog[1].version).isEqualTo("20170106-143015-1234abcd")
        expectedEntries = listOf(ChangelogItemViewModel("internal", "something internal in 2017"))
        assertThat(result.changelog[1].entries).hasSameElementsAs(expectedEntries)

        assertThat(result.changelog[2].date).isEqualTo("Sun Jan 03 2016, 14:30")
        assertThat(result.changelog[2].version).isEqualTo("20160103-143015-1234abcd")
        expectedEntries = listOf(ChangelogItemViewModel("internal", "something internal"),
                ChangelogItemViewModel("public", "something public"))
        assertThat(result.changelog[2].entries).hasSameElementsAs(expectedEntries)
    }

    @Test
    fun `changelogs are ordered by date descending`()
    {
        val sut = ReportController(mockActionContext, mockOrderly)
        val result = sut.getByNameAndVersion()

        assertThat(result.changelog[0].date).isEqualTo("Wed Jan 03 2018, 14:30")
        assertThat(result.changelog[1].date).isEqualTo("Fri Jan 06 2017, 14:30")
        assertThat(result.changelog[2].date).isEqualTo("Sun Jan 03 2016, 14:30")
    }

    @Test
    fun `focalArtefactUrl is null if no artefacts`()
    {
        val sut = ReportController(mockActionContext, mockOrderly)
        val result = sut.getByNameAndVersion()

        assertThat(result.focalArtefactUrl).isNull()
    }

    @Test
    fun `focalArtefactUrl is null if first artefact is not suitable`()
    {
        val unsuitableArtefacts = listOf(Artefact(ArtefactFormat.DATA, "desc",
                listOf(FileInfo("unsuitable.csv", 1), FileInfo("suitable.png",1))))

        val orderly = mock<OrderlyClient> {
            on { this.getDetailsByNameAndVersion("r1", versionId) } doReturn
                    mockReportDetails.copy(artefacts = unsuitableArtefacts)
        }

        val sut = ReportController(mockActionContext, orderly)
        val result = sut.getByNameAndVersion()

        assertThat(result.focalArtefactUrl).isNull()
    }

    @Test
    fun `focalArtefactUrl is url of first artefact if suitable`()
    {
        val artefacts = listOf(Artefact(ArtefactFormat.DATA,
                "desc", listOf(FileInfo("subdir/suitable.png", 1), FileInfo("another.csv", 1))),
                Artefact(ArtefactFormat.DATA, "desc", listOf(FileInfo("unsuitable.csv", 1))))

        val orderly = mock<OrderlyClient> {
            on { this.getDetailsByNameAndVersion("r1", versionId) } doReturn
                    mockReportDetails.copy(artefacts = artefacts)
        }

        val sut = ReportController(mockActionContext, orderly)
        val result = sut.getByNameAndVersion()

        assertThat(result.focalArtefactUrl).isEqualTo("http://localhost:8888/report/r1/version/$versionId/artefacts/subdir%3Asuitable.png?inline=true")
    }

    @Test
    fun `artefacts have expected urls and size`()
    {
        val artefacts = listOf(
                Artefact(ArtefactFormat.DATA, "desc", listOf(FileInfo("unsuitable.csv", 100))),
                Artefact(ArtefactFormat.DATA, "desc", listOf(
                        FileInfo("subdir/another.csv", 200), FileInfo( "suitable.png", 2000))))

        val orderly = mock<OrderlyClient> {
            on { this.getDetailsByNameAndVersion("r1", versionId) } doReturn
                    mockReportDetails.copy(artefacts = artefacts)
        }

        val sut = ReportController(mockActionContext, orderly)
        val result = sut.getByNameAndVersion()

        assertThat(result.artefacts.count()).isEqualTo(2)

        assertThat(result.artefacts[0].artefact).isEqualTo(artefacts[0])
        assertThat(result.artefacts[0].files.count()).isEqualTo(1)
        assertThat(result.artefacts[0].files[0].name).isEqualTo("unsuitable.csv")
        assertThat(result.artefacts[0].files[0].url).isEqualTo("http://localhost:8888/report/r1/version/$versionId/artefacts/unsuitable.csv?inline=false")
        assertThat(result.artefacts[0].files[0].size).isEqualTo(100)

        assertThat(result.artefacts[1].artefact).isEqualTo(artefacts[1])
        assertThat(result.artefacts[1].files.count()).isEqualTo(2)
        assertThat(result.artefacts[1].files[0].name).isEqualTo("subdir/another.csv")
        assertThat(result.artefacts[1].files[0].url).isEqualTo("http://localhost:8888/report/r1/version/$versionId/artefacts/subdir%3Aanother.csv?inline=false")
        assertThat(result.artefacts[1].files[0].size).isEqualTo(200)
        assertThat(result.artefacts[1].files[1].name).isEqualTo("suitable.png")
        assertThat(result.artefacts[1].files[1].url).isEqualTo("http://localhost:8888/report/r1/version/$versionId/artefacts/suitable.png?inline=false")
        assertThat(result.artefacts[1].files[1].size).isEqualTo(2000)
    }

    @Test
    fun `artefacts have expected inline figures`()
    {
        //Expect only the first file in an artefact to be considered for inline figure
        val artefacts = listOf(
                Artefact(ArtefactFormat.DATA, "desc",
                    listOf(FileInfo("unsuitable.csv", 1), FileInfo( "suitable.png", 1))),
                Artefact(ArtefactFormat.DATA, "desc",
                        listOf(FileInfo("suitable.jpg", 1), FileInfo("another.csv", 1))))

        val orderly = mock<OrderlyClient> {
            on { this.getDetailsByNameAndVersion("r1", versionId) } doReturn
                    mockReportDetails.copy(artefacts = artefacts)
        }

        val sut = ReportController(mockActionContext, orderly)
        val result = sut.getByNameAndVersion()

        assertThat(result.artefacts[0].inlineArtefactFigure).isNull()
        assertThat(result.artefacts[1].inlineArtefactFigure).isEqualTo("http://localhost:8888/report/r1/version/$versionId/artefacts/suitable.jpg?inline=true")
    }

    @Test
    fun `dataLinks have expected urls and size`()
    {
        val orderly = mock<OrderlyClient> {
            on { this.getDetailsByNameAndVersion("r1", versionId) } doReturn
                    mockReportDetails.copy(dataInfo = listOf(
                            DataInfo("data1", 100, 1000),
                            DataInfo("data2", 200, 2000)))
        }

        val sut = ReportController(mockActionContext, orderly)
        val result = sut.getByNameAndVersion()

        assertThat(result.dataLinks.count()).isEqualTo(2)

        assertThat(result.dataLinks[0].key).isEqualTo("data1")
        assertThat(result.dataLinks[0].csv.name).isEqualTo("csv")
        assertThat(result.dataLinks[0].csv.url).isEqualTo("http://localhost:8888/report/r1/version/$versionId/data/data1/?type=csv")
        assertThat(result.dataLinks[0].csv.size).isEqualTo(100)
        assertThat(result.dataLinks[0].rds.name).isEqualTo("rds")
        assertThat(result.dataLinks[0].rds.url).isEqualTo("http://localhost:8888/report/r1/version/$versionId/data/data1/?type=rds")
        assertThat(result.dataLinks[0].rds.size).isEqualTo(1000)

        assertThat(result.dataLinks[1].key).isEqualTo("data2")
        assertThat(result.dataLinks[1].csv.name).isEqualTo("csv")
        assertThat(result.dataLinks[1].csv.url).isEqualTo("http://localhost:8888/report/r1/version/$versionId/data/data2/?type=csv")
        assertThat(result.dataLinks[1].csv.size).isEqualTo(200)
        assertThat(result.dataLinks[1].rds.name).isEqualTo("rds")
        assertThat(result.dataLinks[1].rds.url).isEqualTo("http://localhost:8888/report/r1/version/$versionId/data/data2/?type=rds")
        assertThat(result.dataLinks[1].rds.size).isEqualTo(2000)
    }

    @Test
    fun `resources have expected urls and size`()
    {
        val orderly = mock<OrderlyClient> {
            on { this.getDetailsByNameAndVersion("r1", versionId) } doReturn
                    mockReportDetails.copy(resources = listOf(FileInfo("resource1.Rmd", 100), FileInfo( "subdir/resource2.Rmd", 200)))
        }

        val sut = ReportController(mockActionContext, orderly)
        val result = sut.getByNameAndVersion()

        assertThat(result.resources.count()).isEqualTo(2)
        assertThat(result.resources[0].name).isEqualTo("resource1.Rmd")
        assertThat(result.resources[0].url).isEqualTo("http://localhost:8888/report/r1/version/$versionId/resources/resource1.Rmd")
        assertThat(result.resources[0].size).isEqualTo(100)
        assertThat(result.resources[1].name).isEqualTo("subdir/resource2.Rmd")
        assertThat(result.resources[1].url).isEqualTo("http://localhost:8888/report/r1/version/$versionId/resources/subdir%3Aresource2.Rmd")
        assertThat(result.resources[1].size).isEqualTo(200)
    }

    @Test
    fun `zipFile has expected url`()
    {
        val sut = ReportController(mockActionContext, mockOrderly)
        val result = sut.getByNameAndVersion()

        assertThat(result.zipFile.name).isEqualTo("r1-$versionId.zip")
        assertThat(result.zipFile.url).isEqualTo("http://localhost:8888/report/r1/version/$versionId/all/")
        assertThat(result.zipFile.size).isEqualTo(null)
    }

    @Test
    fun `users with report running permissions are report runners`()
    {
        val actionContext = mock<ActionContext> {
            on { this.params(":name") } doReturn "r1"
            on { this.params(":version") } doReturn versionId
            on { this.hasPermission(ReifiedPermission("reports.run", Scope.Global())) } doReturn true
        }
        val sut = ReportController(actionContext, mockOrderly)
        val result = sut.getByNameAndVersion()
        assertThat(result.isRunner).isTrue()
    }

    @Test
    fun `users without report running permissions are not report runners`()
    {
        val actionContext = mock<ActionContext> {
            on { this.params(":name") } doReturn "r1"
            on { this.params(":version") } doReturn versionId
            on { this.hasPermission(ReifiedPermission("reports.run", Scope.Global())) } doReturn false
        }
        val sut = ReportController(actionContext, mockOrderly)
        val result = sut.getByNameAndVersion()
        assertThat(result.isRunner).isFalse()
    }

    @Test
    fun `creates correct breadcrumbs`()
    {
        val sut = ReportController(mockActionContext, mockOrderly)

        val breadcrumbs = sut.getByNameAndVersion().breadcrumbs
        assertThat(breadcrumbs.count()).isEqualTo(2)

        assertThat(breadcrumbs.first().name).isEqualTo("Main menu")
        assertThat(breadcrumbs.first().url).isEqualTo("http://localhost:8888")

        assertThat(breadcrumbs[1].name).isEqualTo("r1 ($versionId)")
        assertThat(breadcrumbs[1].url).isEqualTo("http://localhost:8888/report/r1/$versionId/")
    }

    @Test
    fun `initialises Orderly correctly when user is reviewer`()
    {
        val mockContext = mock<ActionContext> {
            on { this.isReviewer() } doReturn true
        }

        val sut = ReportController(mockContext)

        assertThat((sut.orderly as Orderly).isReviewer).isTrue()
    }

    @Test
    fun `initialises Orderly correctly when user is not reviewer`()
    {
        val mockContext = mock<ActionContext> {
            on { this.isReviewer() } doReturn false
        }

        val sut = ReportController(mockContext)

        assertThat((sut.orderly as Orderly).isReviewer).isFalse()
    }
}