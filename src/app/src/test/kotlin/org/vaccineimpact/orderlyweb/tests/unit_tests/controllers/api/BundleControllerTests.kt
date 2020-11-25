package org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.api

// import com.nhaarman.mockito_kotlin.any
// import com.nhaarman.mockito_kotlin.doReturn
// import com.nhaarman.mockito_kotlin.mock
// import com.nhaarman.mockito_kotlin.verify
// import khttp.responses.Response
// import org.assertj.core.api.Assertions.assertThat
// import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
// import org.vaccineimpact.orderlyweb.ActionContext
// import org.vaccineimpact.orderlyweb.OrderlyServerAPI
// import org.vaccineimpact.orderlyweb.OrderlyServerResponse
// import org.vaccineimpact.orderlyweb.controllers.api.ReportController
// import org.vaccineimpact.orderlyweb.db.Config
// import org.vaccineimpact.orderlyweb.db.OrderlyClient
// import org.vaccineimpact.orderlyweb.db.repositories.ReportRepository
// import org.vaccineimpact.orderlyweb.errors.MissingRequiredPermissionError
// import org.vaccineimpact.orderlyweb.models.ReportVersionWithDescLatest
// import org.vaccineimpact.orderlyweb.models.Changelog
// import org.vaccineimpact.orderlyweb.models.Report
// import org.vaccineimpact.orderlyweb.models.ReportVersionWithDescCustomFieldsLatestParamsTags
// import org.vaccineimpact.orderlyweb.models.permissions.PermissionSet
// import java.time.Instant

class BundleControllerTests : ControllerTest() {
    // private val mockConfig = mock<Config> {
    //     on { this.get("orderly.root") } doReturn "root/"
    //     on { this.authorizationEnabled } doReturn true
    // }

    // private val reportName = "report1"
    // private val reportKey = "123"

    // private val reports = listOf(Report(reportName, "test full name 1", "v1"),
    //         Report("testname2", "test full name 2", "v1"))

    // private val reportVersions = listOf(
    //         ReportVersionWithDescCustomFieldsLatestParamsTags(ReportVersionWithDescLatest(reportName, "display1", "v1", true, Instant.now(), "v1", "desc"),
    //                 mapOf("author" to "auth", "requester" to "req"), mapOf("p1" to "v1"), listOf("t1", "t2")),
    //         ReportVersionWithDescCustomFieldsLatestParamsTags(ReportVersionWithDescLatest("r2", "display2", "v2", true, Instant.now(), "v1", "v2"),
    //                 mapOf("author" to "auth", "requester" to "req"), mapOf("p1" to "v1", "p2" to "v2"), listOf("t1"))
    // )

    // private val mockOrderly = mock<OrderlyClient> {
    //     on { this.getAllReportVersions() } doReturn reportVersions
    // }

    // private val mockReportRepo = mock<ReportRepository> {
    //     on { this.getAllReports() } doReturn reports
    // }

    @Test
    fun `packs a report`() {
        // val actionContext = mock<ActionContext> {
        //     on { this.params(":name") } doReturn reportName
        //     on { this.permissions } doReturn PermissionSet()
        // }

        // val mockAPIResponse = OrderlyServerResponse("okayresponse", 200)

        // val apiClient = mock<OrderlyServerAPI>() {
        //     on { this.post(any(), any()) } doReturn mockAPIResponse
        // }

        // val sut = ReportController(actionContext, mock(), mockReportRepo, apiClient, mockConfig)

        // val result = sut.run()

        // assertThat(result).isEqualTo("okayresponse")
    }

    @Test
    fun `imports a report`() {
        // val actionContext = mock<ActionContext> {
        //     on { this.params(":key") } doReturn reportKey
        // }

        // val mockAPIResponse = OrderlyServerResponse("okayresponse", 200)

        // val apiClient = mock<OrderlyServerAPI>() {
        //     on { this.delete("/v1/reports/$reportKey/kill/", actionContext) } doReturn mockAPIResponse
        // }

        // val sut = ReportController(actionContext, mock(), mockReportRepo, apiClient, mockConfig)
        // val result = sut.kill()

        // assertThat(result).isEqualTo("okayresponse")
    }
}
