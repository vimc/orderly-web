package org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.web

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.web.ReportController
import org.vaccineimpact.orderlyweb.db.Orderly
import org.vaccineimpact.orderlyweb.db.OrderlyClient
import org.vaccineimpact.orderlyweb.models.Artefact
import org.vaccineimpact.orderlyweb.models.ArtefactFormat
import org.vaccineimpact.orderlyweb.models.ReportVersionDetails
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests
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
            mapOf())

    private val mockActionContext = mock<ActionContext> {
        on { this.params(":name") } doReturn "r1"
        on { this.params(":version") } doReturn versionId
    }

    private val mockOrderly = mock<OrderlyClient> {

        on { this.getDetailsByNameAndVersion("r1", versionId) } doReturn mockReportDetails
        on { this.getReportsByName("r1") } doReturn
                listOf(versionId, "20170104-091500-1234dcba")
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

        assertThat(result.versions[1].url).isEqualTo("/reports/r1/20170103-143015-1234abcd")
        assertThat(result.versions[1].date).isEqualTo("Tue Jan 03 2017, 14:30")
        assertThat(result.versions[1].selected).isTrue()

        assertThat(result.versions[0].url).isEqualTo("/reports/r1/20170104-091500-1234dcba")
        assertThat(result.versions[0].date).isEqualTo("Wed Jan 04 2017, 09:15")
        assertThat(result.versions[0].selected).isFalse()
    }

    @Test
    fun `report version picker options are ordered by date descending`()
    {
        val sut = ReportController(mockActionContext, mockOrderly)
        val result = sut.getByNameAndVersion()

        assertThat(result.versions[0].date).isEqualTo("Wed Jan 04 2017, 09:15")
        assertThat(result.versions[1].date).isEqualTo("Tue Jan 03 2017, 14:30")
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
                listOf("unsuitable.csv", "suitable.png")))

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
                "desc", listOf("subdir/suitable.png", "another.csv")),
                Artefact(ArtefactFormat.DATA, "desc", listOf("unsuitable.csv")))

        val orderly = mock<OrderlyClient> {
            on { this.getDetailsByNameAndVersion("r1", versionId) } doReturn
                    mockReportDetails.copy(artefacts = artefacts)
        }

        val sut = ReportController(mockActionContext, orderly)
        val result = sut.getByNameAndVersion()

        assertThat(result.focalArtefactUrl).isEqualTo("/reports/r1/versions/$versionId/artefacts/subdir%3Asuitable.png?inline=true")
    }

    @Test
    fun `artefacts have expected urls`()
    {
        val artefacts = listOf(Artefact(ArtefactFormat.DATA, "desc", listOf("unsuitable.csv")),
                Artefact(ArtefactFormat.DATA, "desc", listOf("subdir/another.csv", "suitable.png")))

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
        assertThat(result.artefacts[0].files[0].url).isEqualTo("/reports/r1/versions/$versionId/artefacts/unsuitable.csv?inline=false")

        assertThat(result.artefacts[1].artefact).isEqualTo(artefacts[1])
        assertThat(result.artefacts[1].files.count()).isEqualTo(2)
        assertThat(result.artefacts[1].files[0].name).isEqualTo("subdir/another.csv")
        assertThat(result.artefacts[1].files[0].url).isEqualTo("/reports/r1/versions/$versionId/artefacts/subdir%3Aanother.csv?inline=false")
        assertThat(result.artefacts[1].files[1].name).isEqualTo("suitable.png")
        assertThat(result.artefacts[1].files[1].url).isEqualTo("/reports/r1/versions/$versionId/artefacts/suitable.png?inline=false")
    }

    @Test
    fun `artefacts have expected inline figures`()
    {
        //Expect only the first file in an artefact to be considered for inline figure
        val artefacts = listOf(Artefact(ArtefactFormat.DATA, "desc", listOf("unsuitable.csv", "suitable.png")),
                Artefact(ArtefactFormat.DATA, "desc", listOf("suitable.jpg", "another.csv")))

        val orderly = mock<OrderlyClient> {
            on { this.getDetailsByNameAndVersion("r1", versionId) } doReturn
                    mockReportDetails.copy(artefacts = artefacts)
        }

        val sut = ReportController(mockActionContext, orderly)
        val result = sut.getByNameAndVersion()

        assertThat(result.artefacts[0].inlineArtefactFigure).isNull()
        assertThat(result.artefacts[1].inlineArtefactFigure).isEqualTo("/reports/r1/versions/$versionId/artefacts/suitable.jpg?inline=true")
    }

    @Test
    fun `dataLinks have expected urls`()
    {
        val orderly = mock<OrderlyClient> {
            on { this.getDetailsByNameAndVersion("r1", versionId) } doReturn
                    mockReportDetails.copy(dataHashes = mapOf("data1" to "1234/567", "data2" to "987&654"))
        }

        val sut = ReportController(mockActionContext, orderly)
        val result = sut.getByNameAndVersion()

        assertThat(result.dataLinks.count()).isEqualTo(2)

        assertThat(result.dataLinks[0].key).isEqualTo("data1")
        assertThat(result.dataLinks[0].csv.name).isEqualTo("csv")
        assertThat(result.dataLinks[0].csv.url).isEqualTo("/reports/r1/versions/$versionId/data/data1/?type=csv")
        assertThat(result.dataLinks[0].rds.name).isEqualTo("rds")
        assertThat(result.dataLinks[0].rds.url).isEqualTo("/reports/r1/versions/$versionId/data/data1/?type=rds")

        assertThat(result.dataLinks[1].key).isEqualTo("data2")
        assertThat(result.dataLinks[1].csv.name).isEqualTo("csv")
        assertThat(result.dataLinks[1].csv.url).isEqualTo("/reports/r1/versions/$versionId/data/data2/?type=csv")
        assertThat(result.dataLinks[1].rds.name).isEqualTo("rds")
        assertThat(result.dataLinks[1].rds.url).isEqualTo("/reports/r1/versions/$versionId/data/data2/?type=rds")
    }

    @Test
    fun `resources have expected urls`()
    {
        val orderly = mock<OrderlyClient> {
            on { this.getDetailsByNameAndVersion("r1", versionId) } doReturn
                    mockReportDetails.copy(resources = listOf("resource1.Rmd", "subdir/resource2.Rmd"))
        }

        val sut = ReportController(mockActionContext, orderly)
        val result = sut.getByNameAndVersion()

        assertThat(result.resources.count()).isEqualTo(2)
        assertThat(result.resources[0].name).isEqualTo("resource1.Rmd")
        assertThat(result.resources[0].url).isEqualTo("/reports/r1/versions/$versionId/resources/resource1.Rmd")
        assertThat(result.resources[1].name).isEqualTo("subdir/resource2.Rmd")
        assertThat(result.resources[1].url).isEqualTo("/reports/r1/versions/$versionId/resources/subdir%3Aresource2.Rmd")
    }

    @Test
    fun `zipFile has expected url`()
    {
        val sut = ReportController(mockActionContext, mockOrderly)
        val result = sut.getByNameAndVersion()

        assertThat(result.zipFile.name).isEqualTo("r1-$versionId.zip")
        assertThat(result.zipFile.url).isEqualTo("/reports/r1/versions/$versionId/all/")
    }

    @Test
    fun `report reviewers are admins`()
    {
        val actionContext = mock<ActionContext> {
            on { this.params(":name") } doReturn "r1"
            on { this.params(":version") } doReturn versionId
            on { this.hasPermission(ReifiedPermission("reports.review", Scope.Global())) } doReturn true
        }
        val sut = ReportController(actionContext, mockOrderly)
        val result = sut.getByNameAndVersion()
        assertThat(result.isAdmin).isTrue()
    }

    @Test
    fun `non report reviewers are not admins`()
    {
        val actionContext = mock<ActionContext> {
            on { this.params(":name") } doReturn "r1"
            on { this.params(":version") } doReturn versionId
            on { this.hasPermission(ReifiedPermission("reports.review", Scope.Global())) } doReturn false
        }
        val sut = ReportController(actionContext, mockOrderly)
        val result = sut.getByNameAndVersion()
        assertThat(result.isAdmin).isFalse()
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
        assertThat(breadcrumbs.first().url).isEqualTo("/")

        assertThat(breadcrumbs[1].name).isEqualTo("r1 ($versionId)")
        assertThat(breadcrumbs[1].url).isEqualTo("/reports/r1/$versionId/")
    }

    @Test
    fun `initialises Orderly correctly when user is reviewer`()
    {
        val mockContext = mock<ActionContext> {
            on { this.hasPermission(ReifiedPermission("reports.review", Scope.Global())) } doReturn true
        }

        val sut = ReportController(mockContext)

        assertThat((sut.orderly as Orderly).isReviewer).isTrue()
    }

    @Test
    fun `initialises Orderly correctly when user is not reviewer`()
    {
        val mockContext = mock<ActionContext> {
            on { this.hasPermission(ReifiedPermission("reports.review", Scope.Global())) } doReturn false
        }

        val sut = ReportController(mockContext)

        assertThat((sut.orderly as Orderly).isReviewer).isFalse()
    }
}