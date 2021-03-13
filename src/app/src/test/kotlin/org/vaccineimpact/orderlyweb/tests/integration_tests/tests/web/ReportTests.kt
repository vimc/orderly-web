package org.vaccineimpact.orderlyweb.tests.integration_tests.tests.web

import com.github.fge.jsonschema.main.JsonValidator
import org.junit.Test
import org.assertj.core.api.Assertions.assertThat
import org.vaccineimpact.orderlyweb.ContentTypes
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.Tables
import org.vaccineimpact.orderlyweb.db.repositories.OrderlyReportRepository
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.test_helpers.insertReport
import org.vaccineimpact.orderlyweb.tests.integration_tests.tests.IntegrationTest
import spark.route.HttpMethod

class ReportTests : IntegrationTest()
{
    @Test
    fun `only report runners can run report`()
    {
        val url = "/report/minimal/actions/run"
        assertWebUrlSecured(url, setOf(ReifiedPermission("reports.run", Scope.Global())),
                method = HttpMethod.post, contentType = ContentTypes.json)
    }

    @Test
    fun `only report runners can get running report status`()
    {
        val url = "/report/minimal/actions/status/frightened_rabbit/"
        val requiredPermissions = setOf(ReifiedPermission("reports.run", Scope.Global()))
        assertWebUrlSecured(url, requiredPermissions, contentType = ContentTypes.json)
    }

    @Test
    fun `only settings managers can set global pinned reports`()
    {
        val url = "/global-pinned-reports/"
        val requiredPermissions = setOf(ReifiedPermission("pinned-reports.manage", Scope.Global()))
        assertWebUrlSecured(url, requiredPermissions, method = HttpMethod.post, contentType = ContentTypes.json,
                postData = mapOf("reports" to listOf<String>()))
    }

    @Test
    fun `can set global pinned reports`()
    {
        val url = "/global-pinned-reports/"

        webRequestHelper.loginWithMontaguAndMakeRequest(url,
                setOf(ReifiedPermission("pinned-reports.manage", Scope.Global())),
                method = HttpMethod.post,
                postData = mapOf("reports" to listOf("html")),
                contentType = ContentTypes.json)

        val pinnedReports = OrderlyReportRepository(true, true).getGlobalPinnedReports()
        assertThat(pinnedReports.count()).isEqualTo(1)
        assertThat(pinnedReports[0].name).isEqualTo("html")
    }

    @Test
    fun `only report reviewers can get report drafts`()
    {
        val url = "/report-drafts/"
        assertWebUrlSecured(url,
                setOf(ReifiedPermission("reports.review", Scope.Global())),
                contentType = ContentTypes.json)
    }

    @Test
    fun `report reviewers can get report drafts`()
    {
        val url = "/report-drafts/"
        val response = webRequestHelper.loginWithMontaguAndMakeRequest(url,
                setOf(ReifiedPermission("reports.review", Scope.Global())),
                contentType = ContentTypes.json)

        val data = JSONValidator.getData(response.text)
        assertThat(data.isArray).isTrue()
        assertThat(data[0].has("display_name")).isTrue()
        assertThat(data[0].has("previously_published")).isTrue()
    }

    @Test
    fun `only report reviewers can publish reports`()
    {
        val url = "/bulk-publish/"
        assertWebUrlSecured(url,
                setOf(ReifiedPermission("reports.review", Scope.Global())),
                method = HttpMethod.post,
                contentType = ContentTypes.json,
                postData = mapOf("ids" to listOf("v1")))
    }

    @Test
    fun `report reviewers can publish reports`()
    {
        insertReport("report", "v1", published = false)
        insertReport("report", "v2", published = false)

        val url = "/bulk-publish/"
        webRequestHelper.loginWithMontaguAndMakeRequest(url,
                setOf(ReifiedPermission("reports.review", Scope.Global())),
                method = HttpMethod.post,
                postData = mapOf("ids" to listOf("v1", "v2")),
                contentType = ContentTypes.json)

        val repo = OrderlyReportRepository(true, true)

        assertThat(repo.getReportVersion("report", "v1").published).isTrue()
        assertThat(repo.getReportVersion("report", "v2").published).isTrue()
    }

    @Test
    fun `only report readers can get report dependencies`()
    {
        val url = "/report/minimal/dependencies/"
        assertWebUrlSecured(url,
                setOf(ReifiedPermission("reports.read", Scope.Global())),
                method = HttpMethod.get,
                contentType = ContentTypes.json)
    }

    @Test
    fun `report readers can get dependencies`()
    {
        val version = JooqContext().use {
            it.dsl.select(Tables.REPORT_VERSION.ID, Tables.REPORT_VERSION.REPORT)
                    .from(Tables.REPORT_VERSION)
                    .fetchAny()
        }

        val versionId = version[Tables.REPORT_VERSION.ID]
        val reportName = version[Tables.REPORT_VERSION.REPORT]

        val url = "/report/$reportName/dependencies/?id=$versionId&direction=upstream"
        val response = webRequestHelper.loginWithMontaguAndMakeRequest(url,
                setOf(ReifiedPermission("reports.read", Scope.Global())),
                method = HttpMethod.get,
                contentType = ContentTypes.json)

        assertSuccessfulWithResponseText(response)
        assertJsonContentType(response)
        JSONValidator.validateAgainstSchema(response.text, "Dependencies")
        val responseData = JSONValidator.getData(response.text)
        assertThat(responseData["direction"]).isEqualTo("upstream")
        assertThat(responseData["dependency_tree"]["name"]).isEqualTo(reportName)
        assertThat(responseData["dependency_tree"]["id"]).isEqualTo(versionId)
    }
}