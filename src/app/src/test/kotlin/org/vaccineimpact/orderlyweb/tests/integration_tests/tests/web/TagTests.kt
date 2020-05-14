package org.vaccineimpact.orderlyweb.tests.integration_tests.tests.web

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.orderlyweb.ContentTypes
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.Tables.*
import org.vaccineimpact.orderlyweb.db.fromJoinPath
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
        val versionTags = getVersionTags(id)
        val reportTags = getReportTags(report)
        assertThat(versionTags).containsExactly("test-tag")
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
                postData = mapOf("report_tags" to listOf("report-test-tag"),
                        "version_tags" to listOf("test-tag")))
    }

    private fun getReportTags(reportName: String): List<String>
    {
        JooqContext().use {
            return it.dsl.select(ORDERLYWEB_REPORT_TAG.TAG)
                    .from(ORDERLYWEB_REPORT_TAG)
                    .where(ORDERLYWEB_REPORT_TAG.REPORT.eq(reportName))
                    .fetchInto(String::class.java)
        }
    }

    private fun getVersionTags(versionId: String): List<String>
    {
        JooqContext().use {
            return it.dsl.select(ORDERLYWEB_REPORT_VERSION_TAG.TAG)
                    .from(ORDERLYWEB_REPORT_VERSION_TAG)
                    .where(ORDERLYWEB_REPORT_VERSION_TAG.REPORT_VERSION.eq(versionId))
                    .fetchInto(String::class.java)
        }
    }

    private fun getAnyReportIds(): Pair<String, String>
    {
        val report = JooqContext().use {

            it.dsl.select(ORDERLYWEB_REPORT_VERSION_FULL.REPORT, ORDERLYWEB_REPORT_VERSION_FULL.ID)
                    .from(ORDERLYWEB_REPORT_VERSION_FULL)
                    .where(ORDERLYWEB_REPORT_VERSION_FULL.PUBLISHED.eq(true))
                    .fetchAny()
        }

        return Pair(report[ORDERLYWEB_REPORT_VERSION_FULL.REPORT], report[ORDERLYWEB_REPORT_VERSION_FULL.ID])
    }
}