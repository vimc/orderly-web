package org.vaccineimpact.orderlyweb.tests.integration_tests.tests.web

import org.assertj.core.api.Assertions.assertThat
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
    fun `report reviewers can tag version`()
    {
        val (report, id) = getAnyReportIds()
        val url = "/report/$report/version/$id/update-tags"
        val result = webRequestHelper.loginWithMontaguAndMakeRequest(url,
                requiredPermissions,
                contentType = ContentTypes.json,
                method = HttpMethod.post,
                postData = mapOf("report_tags" to listOf("report-test-tag"),
                        "version_tags" to listOf("test-tag")))
        assertThat(result.text).isEqualTo("OK")
        val tags = getVersionTags(id)
        val reportTags = getReportTags(report)
        assertThat(tags).containsExactly("test-tag")
        assertThat(reportTags).containsExactly("report-test-tag")
    }

    @Test
    fun `only report reviewers can tag versions`()
    {
        val (report, id) = getAnyReportIds()
        val url = "/report/$report/version/$id/update-tags"
        assertWebUrlSecured(url, requiredPermissions,
                contentType = ContentTypes.json,
                method = HttpMethod.post,
                postData = mapOf("report_tags" to listOf("test-tag"),
                        "version_tags" to listOf("another-tag")))
    }

    private fun getReportTags(reportName: String): List<String>
    {
        JooqContext().use {
            return it.dsl.select(Tables.ORDERLYWEB_REPORT_TAG.TAG)
                    .from(Tables.ORDERLYWEB_REPORT_TAG)
                    .where(Tables.ORDERLYWEB_REPORT_TAG.REPORT.eq(reportName))
                    .fetchInto(String::class.java)
        }
    }

    private fun getVersionTags(versionId: String): List<String>
    {
        JooqContext().use {
            return it.dsl.select(Tables.ORDERLYWEB_REPORT_VERSION_TAG.TAG)
                    .from(Tables.ORDERLYWEB_REPORT_VERSION_TAG)
                    .where(Tables.ORDERLYWEB_REPORT_VERSION_TAG.REPORT_VERSION.eq(versionId))
                    .fetchInto(String::class.java)
        }
    }

    private fun getAnyReportIds(): Pair<String, String>
    {
        val report = JooqContext().use {

            it.dsl.select(Tables.REPORT_VERSION.REPORT, Tables.REPORT_VERSION.ID)
                    .from(Tables.REPORT_VERSION)
                    .where(Tables.REPORT_VERSION.PUBLISHED.eq(true))
                    .fetchAny()
        }

        return Pair(report[Tables.REPORT_VERSION.REPORT], report[Tables.REPORT_VERSION.ID])
    }
}