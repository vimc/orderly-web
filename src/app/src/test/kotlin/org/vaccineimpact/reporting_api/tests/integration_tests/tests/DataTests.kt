package org.vaccineimpact.reporting_api.tests.integration_tests.tests

import org.assertj.core.api.Assertions
import org.junit.Test
import org.vaccineimpact.reporting_api.ContentTypes
import org.vaccineimpact.reporting_api.db.AppConfig
import org.vaccineimpact.reporting_api.tests.insertReport
import java.io.File

class DataTests : IntegrationTest()
{

    @Test
    fun `gets dict of data names to hashes`()
    {

        insertReport("testname", "testversion")
        val response = requestHelper.get("/reports/testname/versions/testversion/data/")

        assertJsonContentType(response)
        assertSuccessful(response)
        JSONValidator.validateAgainstSchema(response.text, "Dictionary")

    }

    @Test
    fun `gets csv data file`()
    {

        var demoCSV = File("${AppConfig()["orderly.root"]}/data/csv/").list()[0]

        // remove file extension
        demoCSV = demoCSV.substring(0, demoCSV.length - 4)

        insertReport("testname", "testversion", hashData = "{ \"testdata\" : \"$demoCSV\"}")

        val url = "/reports/testname/versions/testversion/data/testdata/"
        val token = requestHelper.generateOnetimeToken(url)
        val response = requestHelper.getNoAuth("$url?access_token=$token", ContentTypes.binarydata)

        assertSuccessful(response)
        Assertions.assertThat(response.headers["content-type"]).isEqualTo("text/csv")
        Assertions.assertThat(response.headers["content-disposition"]).isEqualTo("attachment; filename=$demoCSV.csv")
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
        insertReport("testname", "testversion", hashData = "{\"$fakedata\":\"$fakehash\"}")
        val url = "/reports/testname/versions/testversion/data/$fakedata/"
        val token = requestHelper.generateOnetimeToken(url)

        val response = requestHelper.get("$url?access_token=$token", ContentTypes.binarydata)

        assertJsonContentType(response)
        Assertions.assertThat(response.statusCode).isEqualTo(404)
        JSONValidator.validateError(response.text, "file-not-found", "File with name '$fakehash.csv' does not exist")
    }

    @Test
    fun `gets 401 if no access token`()
    {
        val fakedata = "64328fyhdkjs"
        val fakehash = "07dffb003   05279935544238b39d7b14b"
        insertReport("testname", "testversion", hashData = "{\"$fakedata\":\"$fakehash\"}")

        val response = requestHelper.getNoAuth("/reports/testname/versions/testversion/data/$fakedata/", ContentTypes.binarydata)

        Assertions.assertThat(response.statusCode).isEqualTo(401)
        JSONValidator.validateMultipleAuthErrors(response.text)
    }


    @Test
    fun `gets rds data file`()
    {

        var demoRDS = File("${AppConfig()["orderly.root"]}/data/rds/").list()[0]

        // remove file extension
        demoRDS = demoRDS.substring(0, demoRDS.length - 4)

        val url = "/reports/testname/versions/testversion/data/testdata?type=rds"
        val token = requestHelper.generateOnetimeToken(url)

        insertReport("testname", "testversion", hashData = "{ \"testdata\" : \"$demoRDS\"}")
        val response = requestHelper.get("/reports/testname/versions/testversion/data/testdata?type=rds&access_token=$token", ContentTypes.binarydata)

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
        val response = requestHelper.get("$url?access_token=$token", ContentTypes.csv)

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
        val response = requestHelper.get("$url?access_token=$token", ContentTypes.binarydata)

        assertSuccessful(response)
        Assertions.assertThat(response.headers["content-type"]).isEqualTo("application/octet-stream")
        Assertions.assertThat(response.headers["content-disposition"]).isEqualTo("attachment; filename=$demoRDS.rds")
    }

}
