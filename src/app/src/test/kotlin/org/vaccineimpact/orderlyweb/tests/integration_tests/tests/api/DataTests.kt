package org.vaccineimpact.orderlyweb.tests.integration_tests.tests.api

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.vaccineimpact.orderlyweb.ContentTypes
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.test_helpers.insertReport
import org.vaccineimpact.orderlyweb.tests.insertData
import org.vaccineimpact.orderlyweb.tests.integration_tests.helpers.fakeReportReader
import org.vaccineimpact.orderlyweb.tests.integration_tests.tests.IntegrationTest
import java.io.File

class DataTests : IntegrationTest()
{
    @Test
    fun `gets dict of data names to hashes`()
    {
        insertReport("testname", "testversion")
        insertData("testversion", "testdata", "SELECT * FROM thing", "testdb", "123456")
        val response = apiRequestHelper.get(
                "/reports/testname/versions/testversion/data/",
                userEmail = fakeReportReader("testname")
        )

        assertJsonContentType(response)
        assertSuccessful(response)

        val responseData = JSONValidator.getData(response.text)
        Assertions.assertThat(responseData.count()).isEqualTo(1)
        Assertions.assertThat(responseData["testdata"].asText()).isEqualTo("123456")

    }

    @Test
    fun `only report readers can get dict of data names`()
    {
        insertReport("testname", "testversion")
        val url = "/reports/testname/versions/testversion/data/"

        assertAPIUrlSecured(
                url,
                setOf(ReifiedPermission("reports.read", Scope.Specific("report", "testname"))),
                contentType = ContentTypes.json
        )
    }

    @Test
    fun `gets csv data file with scoped permission`()
    {
        var demoCSV = File("${AppConfig()["orderly.root"]}/data/csv/").list()[0]
        demoCSV = demoCSV.substring(0, demoCSV.length - 4)

        insertReport("testname", "testversion")
        insertData("testversion", "testdata", "SELECT * FROM thing", "testdb", demoCSV)

        val url = "/reports/testname/versions/testversion/data/testdata/"
        val response = apiRequestHelper.get(
                url, ContentTypes.csv,
                userEmail = fakeReportReader("testname")
        )

        assertSuccessful(response)
        Assertions.assertThat(response.headers["content-type"]).isEqualTo("text/csv")
        Assertions.assertThat(response.headers["content-disposition"])
                .isEqualTo("attachment; filename=$demoCSV.csv")
    }

    @Test
    fun `only report readers can get csv data file`()
    {
        var demoCSV = File("${AppConfig()["orderly.root"]}/data/csv/").list()[0]
        demoCSV = demoCSV.substring(0, demoCSV.length - 4)

        insertReport("testname", "testversion")
        insertData("testversion", "testdata", "SELECT * FROM thing", "testdb", demoCSV)

        val url = "/reports/testname/versions/testversion/data/testdata/"
        assertAPIUrlSecured(
                url,
                setOf(ReifiedPermission("reports.read", Scope.Specific("report", "testname")))
        )
    }

    @Test
    fun `gets 404 if data doesnt exist in db`()
    {

        insertReport("testname", "testversion")
        val fakedata = "hf647sa674yh3basrhj"
        val url = "/reports/testname/versions/testversion/data/$fakedata/"
        val response = apiRequestHelper.get(url, ContentTypes.binarydata)

        assertJsonContentType(response)
        Assertions.assertThat(response.statusCode).isEqualTo(404)
        JSONValidator.validateError(response.text, "unknown-data", "Unknown data : '$fakedata'")
    }

    @Test
    fun `gets 404 if data file doesnt exist`()
    {

        val fakedata = "64328fyhdkjs"
        val fakehash = "07dffb00305279935544238b39d7b14b"
        insertReport("testname", "testversion")
        insertData("testversion", fakedata, "SELECT * FROM thing", "testdb", fakehash)
        val url = "/reports/testname/versions/testversion/data/$fakedata/"
        val response = apiRequestHelper.get(url, ContentTypes.binarydata)

        assertJsonContentType(response)
        Assertions.assertThat(response.statusCode).isEqualTo(404)
        JSONValidator.validateError(response.text, "file-not-found", "File with name '$fakehash.csv' does not exist")
    }

    @Test
    fun `gets rds data file`()
    {
        var demoRDS = File("${AppConfig()["orderly.root"]}/data/csv/").list()[0]
        demoRDS = demoRDS.substring(0, demoRDS.length - 4)

        insertReport("testname", "testversion")
        insertData("testversion", "testdata", "SELECT * FROM thing", "testdb", demoRDS)

        val url = "/reports/testname/versions/testversion/data/testdata/?type=rds"
        val response = apiRequestHelper.get(url, ContentTypes.binarydata)

        assertSuccessful(response)
        Assertions.assertThat(response.headers["content-type"]).isEqualTo("application/octet-stream")
        Assertions.assertThat(response.headers["content-disposition"]).isEqualTo("attachment; filename=$demoRDS.rds")
    }

    @Test
    fun `gets csv data file by hash`()
    {

        var demoCSV = File("${AppConfig()["orderly.root"]}/data/csv/").list()[0]

        // remove file extension
        demoCSV = demoCSV.substring(0, demoCSV.length - 4)

        val url = "/data/csv/$demoCSV/"
        val response = apiRequestHelper.get(url, ContentTypes.csv)

        assertSuccessful(response)
        Assertions.assertThat(response.headers["content-type"]).isEqualTo("text/csv")
        Assertions.assertThat(response.headers["content-disposition"]).isEqualTo("attachment; filename=$demoCSV.csv")
    }

    @Test
    fun `gets rds data file by hash`()
    {

        var demoRDS = File("${AppConfig()["orderly.root"]}/data/rds/").list()[0]

        // remove file extension
        demoRDS = demoRDS.substring(0, demoRDS.length - 4)

        val url = "/data/rds/$demoRDS/"
        val response = apiRequestHelper.get(url, ContentTypes.binarydata)

        assertSuccessful(response)
        Assertions.assertThat(response.headers["content-type"]).isEqualTo("application/octet-stream")
        Assertions.assertThat(response.headers["content-disposition"]).isEqualTo("attachment; filename=$demoRDS.rds")
    }

}
