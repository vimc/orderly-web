package org.vaccineimpact.reporting_api.tests.integration_tests.tests

import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.reporting_api.ContentTypes
import org.vaccineimpact.reporting_api.db.JooqContext
import org.vaccineimpact.reporting_api.db.Tables
import org.vaccineimpact.reporting_api.security.InternalUser
import org.vaccineimpact.reporting_api.tests.createArchiveFolder
import org.vaccineimpact.reporting_api.tests.deleteArchiveFolder
import org.vaccineimpact.reporting_api.tests.insertReport
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
            val token = requestHelper.generateOnetimeToken(url)
            val response = requestHelper.getNoAuth("$url?access_token=$token", contentType = ContentTypes.zip)

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
            val response = requestHelper.get("/reports/testname/versions/testversion/all/", contentType = ContentTypes.zip,
                    user = InternalUser("testusername", "user", "*/can-login,report:testname/reports.read"))

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

            val token = requestHelper.generateOnetimeToken("")
            val response = requestHelper.get("/reports/testname/versions/testversion/all/?access_token=$token", contentType = ContentTypes.zip)

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
        val response = requestHelper.getNoAuth("/reports/testname/versions/testversion/all", contentType = ContentTypes.zip)

        Assertions.assertThat(response.statusCode).isEqualTo(401)
        JSONValidator.validateMultipleAuthErrors(response.text)
    }

    @Test
    fun `get zip returns 403 if report not in scoped report reading permissions`()
    {
        insertReport("testname", "testversion")
        val response = requestHelper.get("/reports/testname/versions/testversion/all",
                contentType = ContentTypes.zip, user = fakeReportReader("badreportnamer"))

        Assertions.assertThat(response.statusCode).isEqualTo(403)
        JSONValidator.validateError(response.text, "forbidden",
                "You do not have sufficient permissions to access this resource. Missing these permissions: report:testname/reports.read")

    }

    @Test
    fun `get zip returns 404 if report version does not exist`()
    {
        val response = requestHelper.get("/reports/notaname/versions/notareport/all",
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
        val response = requestHelper.get("/reports/use_resource/versions/$version/all/", contentType = ContentTypes.zip,
                user = InternalUser("testusername", "user", "*/can-login,report:use_resource/reports.read"))

        val entries = getZipEntries(response)
        Assertions.assertThat(entries).containsOnly("$version/mygraph.png", "$version/meta/data.csv")
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
        val response = requestHelper.get("/reports/use_resource/versions/$version/all/", contentType = ContentTypes.zip,
                user = InternalUser("testusername", "user", "*/can-login,*/reports.read,*/reports.review"))

        val entries = getZipEntries(response)

        Assertions.assertThat(entries).containsOnly(
                "$version/mygraph.png",
                "$version/meta/data.csv",
                "$version/orderly.yml",
                "$version/orderly_published.yml",
                "$version/orderly_run.rds",
                "$version/orderly_run.yml",
                "$version/script.R")
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