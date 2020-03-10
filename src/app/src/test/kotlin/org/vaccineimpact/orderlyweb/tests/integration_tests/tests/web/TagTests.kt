package org.vaccineimpact.orderlyweb.tests.integration_tests.tests.web

import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.Test
import org.vaccineimpact.orderlyweb.ContentTypes
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.Tables
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.tests.integration_tests.tests.IntegrationTest
import spark.route.HttpMethod

class TagTests : IntegrationTest()
{
    private val requiredPermissions = setOf(ReifiedPermission("reports.review", Scope.Global()))

    @Test
    fun `report reviewers can tag reports`()
    {
        val url = "/report/minimal/tag"
        val result = webRequestHelper.loginWithMontaguAndMakeRequest(url,
                requiredPermissions,
                contentType = ContentTypes.json,
                method = HttpMethod.post,
                postData = mapOf("tag" to "test-tag"))
        assertThat(result.text).isEqualTo("OK")
        assertReportTagExists("minimal", "test-tag")
    }

    @Test
    fun `only report reviewers can tag reports`()
    {
        val url = "/report/minimal/tag"
        assertWebUrlSecured(url, requiredPermissions,
                contentType = ContentTypes.json,
                method = HttpMethod.post,
                postData = mapOf("tag" to "test-tag"))
    }

    @Test
    fun `report reviewers can tag version`()
    {
        val (report, id) = getAnyReportIds()
        val url = "/report/$report/version/$id/tag"
        val result = webRequestHelper.loginWithMontaguAndMakeRequest(url,
                requiredPermissions,
                contentType = ContentTypes.json,
                method = HttpMethod.post,
                postData = mapOf("tag" to "test-tag"))
        assertThat(result.text).isEqualTo("OK")
        assertVersionTagExists("v1", "test-tag")
    }

    @Test
    fun `only report reviewers can tag versions`()
    {
        val (report, id) = getAnyReportIds()
        val url = "/report/$report/version/$id/tag"
        assertWebUrlSecured(url, requiredPermissions,
                contentType = ContentTypes.json,
                method = HttpMethod.post,
                postData = mapOf("tag" to "test-tag"))
    }

    private fun assertReportTagExists(reportName: String, tag: String)
    {
        val tags = JooqContext().use {
            it.dsl.select(Tables.ORDERLYWEB_REPORT_TAG.TAG)
                    .from(Tables.ORDERLYWEB_REPORT_TAG)
                    .where(Tables.ORDERLYWEB_REPORT_TAG.REPORT.eq(reportName))
                    .fetchInto(String::class.java)
        }
        assertThat(tags.first()).isEqualTo(tag)
    }

    private fun assertVersionTagExists(versionId: String, tag: String)
    {
        val tags = JooqContext().use {
            it.dsl.select(Tables.ORDERLYWEB_REPORT_VERSION_TAG.TAG)
                    .from(Tables.ORDERLYWEB_REPORT_VERSION_TAG)
                    .where(Tables.ORDERLYWEB_REPORT_VERSION_TAG.REPORT_VERSION.eq(versionId))
                    .fetchInto(String::class.java)
        }
        assertThat(tags.first()).isEqualTo(tag)
    }

    private fun getAnyReportIds(): Pair<String, String>
    {
        val report = JooqContext().use {

            it.dsl.select(Tables.REPORT_VERSION.REPORT, Tables.REPORT_VERSION.ID)
                    .where(Tables.REPORT_VERSION.PUBLISHED.eq(true))
                    .fetchAny()
        }

        return Pair(report[Tables.REPORT_VERSION.REPORT], report[Tables.REPORT_VERSION.ID])
    }
}