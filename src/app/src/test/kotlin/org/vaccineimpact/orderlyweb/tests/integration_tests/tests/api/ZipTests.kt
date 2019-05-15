package org.vaccineimpact.orderlyweb.tests.integration_tests.tests.api

import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.orderlyweb.ContentTypes
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.Tables
import org.vaccineimpact.orderlyweb.test_helpers.insertReport
import org.vaccineimpact.orderlyweb.tests.createArchiveFolder
import org.vaccineimpact.orderlyweb.tests.deleteArchiveFolder
import org.vaccineimpact.orderlyweb.tests.integration_tests.helpers.fakeGlobalReportReader
import org.vaccineimpact.orderlyweb.tests.integration_tests.helpers.fakeGlobalReportReviewer
import org.vaccineimpact.orderlyweb.tests.integration_tests.helpers.fakeReportReader
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
    fun `gets zip file with scoped permissions`()
    {
        insertReport("testname", "testversion")
        createArchiveFolder("testname", "testversion")

        try
        {
            val response = apiRequestHelper.get("/reports/testname/versions/testversion/all/", contentType = ContentTypes.zip,
                    userEmail = fakeReportReader("testname"))

            assertSuccessful(response)
            assertThat(response.headers["content-type"]).isEqualTo("application/zip")
            assertThat(response.headers["content-disposition"]).isEqualTo("attachment; filename=testname/testversion.zip")
        } finally
        {
            deleteArchiveFolder("testname", "testversion")
        }

    }

    @Test
    fun `gets zip file with bearer token`()
    {
        insertReport("testname", "testversion")
        createArchiveFolder("testname", "testversion")

        try
        {
            val response = apiRequestHelper.get("/reports/testname/versions/testversion/all/",
                    contentType = ContentTypes.zip)

            assertSuccessful(response)
            assertThat(response.headers["content-type"]).isEqualTo("application/zip")
            assertThat(response.headers["content-disposition"]).isEqualTo("attachment; filename=testname/testversion.zip")
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

        Assertions.assertThat(response.statusCode).isEqualTo(401)
        JSONValidator.validateMultipleAuthErrors(response.text)
    }

    @Test
    fun `get zip returns 403 if report not in scoped report reading permissions`()
    {
        insertReport("testname", "testversion")
        val response = apiRequestHelper.get("/reports/testname/versions/testversion/all",
                contentType = ContentTypes.zip,  userEmail = "user@email.com")

        Assertions.assertThat(response.statusCode).isEqualTo(403)
        JSONValidator.validateError(response.text, "forbidden",
                "You do not have sufficient permissions to access this resource. Missing these permissions: report:testname/reports.read")

    }

    @Test
    fun `get zip returns 404 if report version does not exist`()
    {
        val response = apiRequestHelper.get("/reports/notaname/versions/notareport/all",
                contentType = ContentTypes.zip)

        Assertions.assertThat(response.statusCode).isEqualTo(404)
        JSONValidator.validateError(response.text, "unknown-report-version",
                "Unknown report-version")

    }

    @Test
    fun `report reader gets only artefacts and resources`()
    {
        val version = JooqContext("demo/orderly.sqlite").use {
            it.dsl.select(Tables.REPORT_VERSION.ID)
                    .from(Tables.REPORT_VERSION)
                    .where(Tables.REPORT_VERSION.REPORT.eq("use_resource"))
                    .and(Tables.REPORT_VERSION.PUBLISHED.eq(true))
                    .fetchInto(String::class.java)
                    .first()
        }
        val response = apiRequestHelper.get("/reports/use_resource/versions/$version/all/", contentType = ContentTypes.zip,
                userEmail = fakeGlobalReportReader())

        val entries = getZipEntries(response)
        Assertions.assertThat(entries).containsOnly("$version/mygraph.png", "$version/meta/data.csv", "$version/README.md")
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

        Assertions.assertThat(entries).containsOnly(
                "$version/mygraph.png",
                "$version/meta/data.csv",
                "$version/orderly.yml",
                "$version/orderly_published.yml",
                "$version/orderly_run.rds",
                "$version/orderly_run.yml",
                "$version/script.R",
                "$version/README.md")
    }

    private fun getZipEntries(response: khttp.responses.Response): MutableList<String>
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