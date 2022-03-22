package org.vaccineimpact.orderlyweb.tests.integration_tests.tests.api

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.orderlyweb.ContentTypes
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.Tables
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.test_helpers.http.Response
import org.vaccineimpact.orderlyweb.test_helpers.insertReport
import org.vaccineimpact.orderlyweb.tests.createArchiveFolder
import org.vaccineimpact.orderlyweb.tests.deleteArchiveFolder
import org.vaccineimpact.orderlyweb.tests.integration_tests.helpers.fakeGlobalReportReader
import org.vaccineimpact.orderlyweb.tests.integration_tests.helpers.fakeGlobalReportReviewer
import org.vaccineimpact.orderlyweb.tests.integration_tests.tests.IntegrationTest
import java.io.ByteArrayInputStream
import java.util.zip.ZipInputStream

class ZipTests : IntegrationTest()
{

    @Test
    fun `gets zip file with access token`()
    {

        insertReport("testname", "testversion")
        createArchiveFolder("testname", "testversion")

        try
        {
            val url = "/reports/testname/versions/testversion/all/"
            val token = apiRequestHelper.generateOnetimeToken(url)
            val response = apiRequestHelper.getNoAuth("$url?access_token=$token", contentType = ContentTypes.zip)

            assertSuccessful(response)
            assertThat(response.headers["content-type"]).isEqualTo("application/zip")
            assertThat(response.headers["content-disposition"]).isEqualTo("attachment; filename=testname/testversion.zip")
        } finally
        {
            deleteArchiveFolder("testname", "testversion")
        }
    }

    @Test
    fun `only report readers can get zip file`()
    {
        insertReport("testname", "testversion")
        createArchiveFolder("testname", "testversion")

        try
        {
            val url = "/reports/testname/versions/testversion/all/"
            assertAPIUrlSecured(url,
                    setOf(ReifiedPermission("reports.read", Scope.Specific("report", "testname"))),
                    contentType = ContentTypes.zip)
        } finally
        {
            deleteArchiveFolder("testname", "testversion")
        }
    }

    @Test
    fun `get zip returns 401 if access token is missing`()
    {
        insertReport("testname", "testversion")
        val response = apiRequestHelper.getNoAuth("/reports/testname/versions/testversion/all", contentType = ContentTypes.zip)

        assertThat(response.statusCode).isEqualTo(401)
        JSONValidator.validateMultipleAuthErrors(response.text)
    }

    @Test
    fun `get zip returns 404 if report version does not exist`()
    {
        val response = apiRequestHelper.get("/reports/notaname/versions/notareport/all",
                contentType = ContentTypes.zip)

        assertThat(response.statusCode).isEqualTo(404)
        JSONValidator.validateError(response.text, "unknown-report-version",
                "Unknown report-version")
    }

    @Test
    fun `report reader gets only artefacts and resources`()
    {
        val version = JooqContext("demo/orderly.sqlite").use {
            it.dsl.select(Tables.ORDERLYWEB_REPORT_VERSION_FULL.ID)
                    .from(Tables.ORDERLYWEB_REPORT_VERSION_FULL)
                    .where(Tables.ORDERLYWEB_REPORT_VERSION_FULL.REPORT.eq("use_resource"))
                    .and(Tables.ORDERLYWEB_REPORT_VERSION_FULL.PUBLISHED.eq(true))
                    .fetchInto(String::class.java)
                    .first()
        }
        val response = apiRequestHelper.get("/reports/use_resource/versions/$version/all/", contentType = ContentTypes.zip,
                userEmail = fakeGlobalReportReader())

        val entries = getZipEntries(response)
        assertThat(entries).containsOnly("$version/mygraph.png", "$version/meta/data.csv", "$version/README.md")
    }

    @Test
    fun `report reviewer gets whole directory`()
    {
        val version = JooqContext("demo/orderly.sqlite").use {
            it.dsl.select(Tables.REPORT_VERSION.ID)
                    .from(Tables.REPORT_VERSION)
                    .where(Tables.REPORT_VERSION.REPORT.eq("use_resource"))
                    .fetchInto(String::class.java)
                    .first()
        }
        val response = apiRequestHelper.get("/reports/use_resource/versions/$version/all/",
                contentType = ContentTypes.zip,
                userEmail = fakeGlobalReportReviewer())

        val entries = getZipEntries(response)

        assertThat(entries).containsOnly(
                "$version/mygraph.png",
                "$version/meta/data.csv",
                "$version/orderly.yml",
                "$version/orderly_published.yml",
                "$version/orderly_run.rds",
                "$version/script.R",
                "$version/README.md")
    }

    private fun getZipEntries(response: Response): MutableList<String>
    {
        val entries = mutableListOf<String>()
        ZipInputStream(ByteArrayInputStream(response.content)).use {
            var entry = it.nextEntry
            while (entry != null)
            {
                entries.add(entry.name)
                entry = it.nextEntry
            }
        }

        return entries
    }
}
