package org.vaccineimpact.orderlyweb.tests.integration_tests.tests.web

import java.net.URLEncoder
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.Tables
import org.vaccineimpact.orderlyweb.db.joinPath
import org.vaccineimpact.orderlyweb.tests.integration_tests.helpers.fakeGlobalReportReader
import org.vaccineimpact.orderlyweb.tests.integration_tests.helpers.fakeGlobalReportReviewer
import org.vaccineimpact.orderlyweb.tests.integration_tests.tests.IntegrationTest

class VersionTests : IntegrationTest()
{
    @Test
    fun `report reviewers have permission to publish report version`()
    {
        val version = JooqContext("git/orderly.sqlite").use {

            it.dsl.select(Tables.REPORT_VERSION.ID, Tables.REPORT_VERSION.REPORT)
                    .from(Tables.REPORT_VERSION)
                    .fetchAny()
        }

        val versionId = version[Tables.REPORT_VERSION.ID]
        val reportName = version[Tables.REPORT_VERSION.REPORT]

        val response = requestHelper.post("/reports/$reportName/versions/$versionId/publish/", mapOf(),
                userEmail = fakeGlobalReportReviewer())

        // we don't care whether this is successful or not, just whether the user is authorized
        assertThat(response.statusCode).isNotEqualTo(403)
    }

    @Test
    fun `non report reviewers do not have permission to publish report version`()
    {
        val version = JooqContext("git/orderly.sqlite").use {

            it.dsl.select(Tables.REPORT_VERSION.ID, Tables.REPORT_VERSION.REPORT)
                    .from(Tables.REPORT_VERSION)
                    .fetchAny()
        }

        val versionId = version[Tables.REPORT_VERSION.ID]
        val reportName = version[Tables.REPORT_VERSION.REPORT]

        val response = requestHelper.post("/reports/$reportName/versions/$versionId/publish/", mapOf(),
                userEmail = fakeGlobalReportReader())

        assertThat(response.statusCode).isEqualTo(403)
    }

    @Test
    fun `report readers can get resource`()
    {
        var url = getAnyResourceUrl()

        val response = requestHelper.post(url, mapOf(), userEmail = fakeGlobalReportReader())

        assertThat(response.statusCode).isEqualTo(200)

    }

    @Test
    fun `non report readers cannot get resource`()
    {
        var url = getAnyResourceUrl()

        val response = requestHelper.post(url, mapOf(), userEmail = "no@permissions.com")

        assertThat(response.statusCode).isEqualTo(403)
    }

    @Test
    fun `report readers can get zip file`()
    {
        val version = JooqContext("git/orderly.sqlite").use {

            it.dsl.select(Tables.REPORT_VERSION.ID, Tables.REPORT_VERSION.REPORT)
                    .from(Tables.REPORT_VERSION)
                    .fetchAny()
        }

        val versionId = version[Tables.REPORT_VERSION.ID]
        val reportName = version[Tables.REPORT_VERSION.REPORT]

        val response = requestHelper.post("/reports/$reportName/versions/$versionId/all/", mapOf(),
                userEmail = fakeGlobalReportReader())

        assertThat(response.statusCode).isEqualTo(200)
    }

    @Test
    fun `non report readers cannot get zip file`()
    {
        val version = JooqContext("git/orderly.sqlite").use {

            it.dsl.select(Tables.REPORT_VERSION.ID, Tables.REPORT_VERSION.REPORT)
                    .from(Tables.REPORT_VERSION)
                    .fetchAny()
        }

        val versionId = version[Tables.REPORT_VERSION.ID]
        val reportName = version[Tables.REPORT_VERSION.REPORT]

        val response = requestHelper.post("/reports/$reportName/versions/$versionId/all/", mapOf(),
                userEmail = "no@permissions.com")

        assertThat(response.statusCode).isEqualTo(403)
    }

    private fun getAnyResourceUrl(): String
    {
        val resource = JooqContext("git/orderly.sqlite").use {

            it.dsl.select(Tables.FILE_INPUT.FILENAME, Tables.FILE_INPUT.REPORT_VERSION, Tables.REPORT_VERSION.REPORT)
                    .from(Tables.FILE_INPUT)
                    .joinPath(Tables.REPORT_VERSION)
                    .fetchAny()
        }

        val report = resource[Tables.REPORT_VERSION.REPORT]
        val version = resource[Tables.FILE_INPUT.REPORT_VERSION]
        val fileName = resource[Tables.FILE_INPUT.FILENAME]
        val encodedFileName = URLEncoder.encode(fileName, "UTF-8")

        return "/reports/$report/versions/$version/resources/$encodedFileName/"
    }
}