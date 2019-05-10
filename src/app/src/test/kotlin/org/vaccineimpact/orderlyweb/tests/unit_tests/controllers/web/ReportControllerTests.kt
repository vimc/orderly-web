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
    private val mockReportDetails = ReportVersionDetails("r1",
            "a fake report",
            "v1",
            true,
            Instant.now(),
            "dr author",
            "ms funder",
            "a fake report",
            listOf(),
            listOf(),
            mapOf())

    private val actionContext = mock<ActionContext> {
        on { this.params(":name") } doReturn "r1"
        on { this.params(":version") } doReturn "v1"
    }

    @Test
    fun `getByNameAndVersion returns display name if not present`()
    {
        val orderly = mock<OrderlyClient> {
            on { this.getDetailsByNameAndVersion("r1", "v1") } doReturn mockReportDetails
        }

        val sut = ReportController(actionContext, orderly)
        val result = sut.getByNameAndVersion()

        assertThat(result.report).isEqualTo(mockReportDetails)
    }

    @Test
    fun `getByNameAndVersion uses name for display name if not present`()
    {

        val orderly = mock<OrderlyClient> {
            on { this.getDetailsByNameAndVersion("r1", "v1") } doReturn
                    mockReportDetails.copy(displayName = null)
        }

        val sut = ReportController(actionContext, orderly)
        val result = sut.getByNameAndVersion()

        assertThat(result.report.displayName).isEqualTo("r1")
    }

    @Test
    fun `focalArtefactUrl is null if no artefacts`()
    {
        val orderly = mock<OrderlyClient> {
            on { this.getDetailsByNameAndVersion("r1", "v1") } doReturn mockReportDetails
        }

        val sut = ReportController(actionContext, orderly)
        val result = sut.getByNameAndVersion()

        assertThat(result.focalArtefactUrl).isNull()
    }

    @Test
    fun `focalArtefactUrl is null if no suitable artefact`()
    {
        val unsuitableArtefacts = listOf(Artefact(ArtefactFormat.DATA, "desc", listOf("unsuitable.csv")))
        val orderly = mock<OrderlyClient> {
            on { this.getDetailsByNameAndVersion("r1", "v1") } doReturn
                    mockReportDetails.copy(artefacts = unsuitableArtefacts)
        }

        val sut = ReportController(actionContext, orderly)
        val result = sut.getByNameAndVersion()

        assertThat(result.focalArtefactUrl).isNull()
    }

    @Test
    fun `focalArtefactUrl is url of first suitable artefact`()
    {
        val artefacts = listOf(Artefact(ArtefactFormat.DATA, "desc", listOf("unsuitable.csv")),
                Artefact(ArtefactFormat.DATA, "desc", listOf("another.csv", "suitable.png")))

        val orderly = mock<OrderlyClient> {
            on { this.getDetailsByNameAndVersion("r1", "v1") } doReturn
                    mockReportDetails.copy(artefacts = artefacts)
        }

        val sut = ReportController(actionContext, orderly)
        val result = sut.getByNameAndVersion()

        assertThat(result.focalArtefactUrl).isEqualTo("/reports/r1/versions/v1/artefacts/suitable.png?inline=true")
    }

    @Test
    fun `report reviewers are admins`()
    {
        val orderly = mock<OrderlyClient> {
            on { this.getDetailsByNameAndVersion("r1", "v1") } doReturn
                    mockReportDetails
        }
        val actionContext = mock<ActionContext> {
            on { this.params(":name") } doReturn "r1"
            on { this.params(":version") } doReturn "v1"
            on { this.hasPermission(ReifiedPermission("reports.review", Scope.Global())) } doReturn true
        }
        val sut = ReportController(actionContext, orderly)
        val result = sut.getByNameAndVersion()
        assertThat(result.isAdmin).isTrue()
    }

    @Test
    fun `non report reviewers are not admins`()
    {
        val orderly = mock<OrderlyClient> {
            on { this.getDetailsByNameAndVersion("r1", "v1") } doReturn
                    mockReportDetails
        }
        val actionContext = mock<ActionContext> {
            on { this.params(":name") } doReturn "r1"
            on { this.params(":version") } doReturn "v1"
            on { this.hasPermission(ReifiedPermission("reports.review", Scope.Global())) } doReturn false
        }
        val sut = ReportController(actionContext, orderly)
        val result = sut.getByNameAndVersion()
        assertThat(result.isAdmin).isFalse()
    }

    @Test
    fun `images can render in browser`()
    {
        val sut = ReportController(actionContext, mock())
        val png = sut.canRenderInBrowser("test.png")
        val gif = sut.canRenderInBrowser("test.gif")
        val jpeg = sut.canRenderInBrowser("test.jpeg")
        val jpg = sut.canRenderInBrowser("test.jpg")
        val JPG = sut.canRenderInBrowser("test.JPG")
        val svg = sut.canRenderInBrowser("test.svg")

        assertThat(png).isTrue()
        assertThat(gif).isTrue()
        assertThat(jpg).isTrue()
        assertThat(jpeg).isTrue()
        assertThat(JPG).isTrue()
        assertThat(svg).isTrue()
    }

    @Test
    fun `html can render in browser`()
    {
        val sut = ReportController(actionContext, mock())
        val html = sut.canRenderInBrowser("test.html")
        val htm = sut.canRenderInBrowser("test.htm")

        assertThat(html).isTrue()
        assertThat(htm).isTrue()
    }

    @Test
    fun `pdf can render in browser`()
    {
        val sut = ReportController(actionContext, mock())
        val pdf = sut.canRenderInBrowser("test.pdf")

        assertThat(pdf).isTrue()
    }

    @Test
    fun `initialises Orderly correctly when user is reviewer`()
    {
        val mockContext = mock<ActionContext> {
            on { this.hasPermission(ReifiedPermission("reports.review", Scope.Global()))} doReturn true
        }

        val sut = ReportController(mockContext)

        assertThat((sut.orderly as Orderly).isReviewer).isTrue()
    }

    @Test
    fun `initialises Orderly correctly when user is not reviewer`()
    {
        val mockContext = mock<ActionContext> {
            on { this.hasPermission(ReifiedPermission("reports.review", Scope.Global()))} doReturn false
        }

        val sut = ReportController(mockContext)

        assertThat((sut.orderly as Orderly).isReviewer).isFalse()
    }
}