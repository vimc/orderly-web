package org.vaccineimpact.reporting_api.tests.integration_tests.tests

import org.assertj.core.api.Assertions
import org.junit.Test
import org.vaccineimpact.reporting_api.ContentTypes
import org.vaccineimpact.reporting_api.db.AppConfig
import org.vaccineimpact.reporting_api.tests.insertData
import org.vaccineimpact.reporting_api.tests.insertReport
import java.io.File

class DataTests : IntegrationTest()
{

    @Test
    fun `gets dict of data names to hashes with scoped report reading permission`()
    {
        insertReport("testname", "testversion")
        insertData("testversion", "testdata", "SELECT * FROM thing", "123456")
        val response = requestHelper.get("/reports/testname/versions/testversion/data/",
                user = fakeReportReader("testname"))

        assertJsonContentType(response)
        assertSuccessful(response)

        val responseData = JSONValidator.getData(response.text)
        Assertions.assertThat(responseData.count()).isEqualTo(1)
        Assertions.assertThat(responseData["testdata"].asText()).isEqualTo("123456")

    }

    @Test
    fun `can't get dict of data names if report not within report reading scope`()
    {
        insertReport("testname", "testversion")
        val response = requestHelper.get("/reports/testname/versions/testversion/data/",
                user = fakeReportReader("badreportname"))

        assertUnauthorized(response, "testname")
    }

    @Test
    fun `gets csv data file with scoped permission`()
    {
        var demoCSV = File("${AppConfig()["orderly.root"]}/data/csv/").list()[0]

        // remove file extension
        demoCSV = demoCSV.substring(0, demoCSV.length - 4)

        insertReport("testname", "testversion")
        insertData("testversion", "testdata", "SELECT * FROM thing", demoCSV)

        val url = "/reports/testname/versions/testversion/data/testdata/"
        val response = requestHelper.get(url, ContentTypes.binarydata, user = fakeReportReader("testname"))

        assertSuccessful(response)
        Assertions.assertThat(response.headers["content-type"]).isEqualTo("text/csv")
        Assertions.assertThat(response.headers["content-disposition"]).isEqualTo("attachment; filename=$demoCSV.csv")
    }

    @Test
    fun `can't get csv data file if report not in scoped permissions`()
    {

        var demoCSV = File("${AppConfig()["orderly.root"]}/data/csv/").list()[0]

        // remove file extension
        demoCSV = demoCSV.substring(0, demoCSV.length - 4)

        insertReport("testname", "testversion")
        insertData("testversion", "testdata", "SELECT * FROM thing", demoCSV)

        val url = "/reports/testname/versions/testversion/data/testdata/"
        val response = requestHelper.get(url, ContentTypes.binarydata,
                user = fakeReportReader("badreportname"))

        assertUnauthorized(response, "testname")
    }

    @Test
    fun `gets 404 if data doesnt exist in db`()
    {

        insertReport("testname", "testversion")
        val fakedata = "hf647sa674yh3basrhj"
        val url = "/reports/testname/versions/testversion/data/$fakedata/"
        val token = requestHelper.generateOnetimeToken(url)
        val response = requestHelper.get("$url?access_token=$token", ContentTypes.binarydata)

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
        insertData("testversion", fakedata, "SELECT * FROM thing", fakehash)
        val url = "/reports/testname/versions/testversion/data/$fakedata/"
        val token = requestHelper.generateOnetimeToken(url)

        val response = requestHelper.getNoAuth("$url?access_token=$token", ContentTypes.binarydata)

        assertJsonContentType(response)
        Assertions.assertThat(response.statusCode).isEqualTo(404)
        JSONValidator.validateError(response.text, "file-not-found", "File with name '$fakehash.csv' does not exist")
    }


    @Test
    fun `gets rds data file`()
    {
        var demoRDS = File("${AppConfig()["orderly.root"]}/data/rds/").list()[0]

        // remove file extension
        demoRDS = demoRDS.substring(0, demoRDS.length - 4)

        insertReport("testname", "testversion")
        insertData("testversion", "testdata", "SELECT * FROM thing", demoRDS)

        val url = "/reports/testname/versions/testversion/data/testdata/?type=rds"
        val token = requestHelper.generateOnetimeToken(url)
        val response = requestHelper.getNoAuth("$url&access_token=$token", ContentTypes.binarydata)

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
        val token = requestHelper.generateOnetimeToken(url)
        val response = requestHelper.getNoAuth("$url?access_token=$token", ContentTypes.csv)

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
        val token = requestHelper.generateOnetimeToken(url)
        val response = requestHelper.getNoAuth("$url?access_token=$token", ContentTypes.binarydata)

        assertSuccessful(response)
        Assertions.assertThat(response.headers["content-type"]).isEqualTo("application/octet-stream")
        Assertions.assertThat(response.headers["content-disposition"]).isEqualTo("attachment; filename=$demoRDS.rds")
    }

}
