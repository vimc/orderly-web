package org.vaccineimpact.orderlyweb.tests.integration_tests.tests.web

import java.net.URLEncoder
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.Tables
import org.vaccineimpact.orderlyweb.db.joinPath
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.tests.integration_tests.helpers.fakeGlobalReportReader
import org.vaccineimpact.orderlyweb.tests.integration_tests.helpers.fakeGlobalReportReviewer
import org.vaccineimpact.orderlyweb.tests.integration_tests.tests.IntegrationTest
import spark.route.HttpMethod

class VersionTests : IntegrationTest()
{
    @Test
    fun `only report reviewers can publish report version`()
    {
        val version = JooqContext("git/orderly.sqlite").use {

            it.dsl.select(Tables.REPORT_VERSION.ID, Tables.REPORT_VERSION.REPORT)
                    .from(Tables.REPORT_VERSION)
                    .fetchAny()
        }

        val versionId = version[Tables.REPORT_VERSION.ID]
        val reportName = version[Tables.REPORT_VERSION.REPORT]

        val url = "/reports/$reportName/versions/$versionId/publish/"

        assertWebUrlSecured(url, setOf(ReifiedPermission("reports.review", Scope.Global())), method = HttpMethod.post)
    }


    @Test
    fun `only report readers can get resource`()
    {
        var url = getAnyResourceUrl()

        assertWebUrlSecured(url, setOf(ReifiedPermission("reports.read", Scope.Global())))

    }

    @Test
    fun `only report readers can get zip file`()
    {
        val version = JooqContext("git/orderly.sqlite").use {

            it.dsl.select(Tables.REPORT_VERSION.ID, Tables.REPORT_VERSION.REPORT)
                    .from(Tables.REPORT_VERSION)
                    .fetchAny()
        }

        val versionId = version[Tables.REPORT_VERSION.ID]
        val reportName = version[Tables.REPORT_VERSION.REPORT]

        val url = "/reports/$reportName/versions/$versionId/all/"

        assertWebUrlSecured(url, setOf(ReifiedPermission("reports.read", Scope.Global())))
    }


    private fun getAnyResourceUrl(): String
    {
        val resource = JooqContext("git/orderly.sqlite").use {

            it.dsl.select(Tables.FILE_INPUT.FILENAME, Tables.FILE_INPUT.REPORT_VERSION, Tables.REPORT_VERSION.REPORT)
                    .from(Tables.FILE_INPUT)
                    .join(Tables.REPORT_VERSION)
                    .on(Tables.FILE_INPUT.REPORT_VERSION.eq(Tables.REPORT_VERSION.ID))
                    .fetchAny()
        }

        val report = resource[Tables.REPORT_VERSION.REPORT]
        val version = resource[Tables.FILE_INPUT.REPORT_VERSION]
        val fileName = resource[Tables.FILE_INPUT.FILENAME]
        val encodedFileName = URLEncoder.encode(fileName, "UTF-8")

        return "/reports/$report/versions/$version/resources/$encodedFileName/"
    }
}