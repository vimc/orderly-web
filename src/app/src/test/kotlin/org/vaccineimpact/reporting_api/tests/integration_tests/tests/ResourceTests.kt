package org.vaccineimpact.reporting_api.tests.integration_tests.tests

import org.assertj.core.api.Assertions
import org.junit.Test
import org.vaccineimpact.reporting_api.ContentTypes
import org.vaccineimpact.reporting_api.db.Config
import org.vaccineimpact.reporting_api.tests.insertReport
import java.io.File
import java.net.URLEncoder

class ResourceTests : IntegrationTest()
{
    @Test
    fun `gets dict of resource names to hashes`()
    {
        insertReport("testname", "testversion")
        val response = requestHelper.get("/reports/testname/testversion/resources")

        assertJsonContentType(response)
        assertSuccessful(response)
        JSONValidator.validateAgainstSchema(response.text, "Dictionary")

    }

    @Test
    fun `gets resource file`()
    {
        val version = File("${Config["orderly.root"]}/archive/use_resource/").list()[0]

        val resourceEncoded = URLEncoder.encode("meta/data.csv", "UTF-8")
        val url = "/reports/use_resource/$version/resources/$resourceEncoded/"
        val token = requestHelper.generateOnetimeToken(url)
        val response = requestHelper.get("$url?access_token=$token", ContentTypes.binarydata)

        assertSuccessful(response)
        Assertions.assertThat(response.headers["content-type"]).isEqualTo("application/octet-stream")
        Assertions.assertThat(response.headers["content-disposition"]).isEqualTo("attachment; filename=use_resource/$version/meta/data.csv")

    }

    @Test
    fun `gets 404 if resource doesnt exist in db`()
    {
        insertReport("testname", "testversion")
        val fakeresource = "hf647rhj"
        val url = "/reports/testname/testversion/resources/$fakeresource/"
        val token = requestHelper.generateOnetimeToken(url)
        val response = requestHelper.get("/reports/testname/testversion/resources/$fakeresource/?access_token=$token", ContentTypes.binarydata)

        assertJsonContentType(response)
        Assertions.assertThat(response.statusCode).isEqualTo(404)
        JSONValidator.validateError(response.text, "unknown-resource", "Unknown resource : '$fakeresource'")
    }

    @Test
    fun `gets 401 if missing access token`()
    {
        insertReport("testname", "testversion")
        val fakeresource = "hf647rhj"
        val response = requestHelper.getNoAuth("/reports/testname/testversion/resources/$fakeresource/", ContentTypes.binarydata)

        Assertions.assertThat(response.statusCode).isEqualTo(401)
        JSONValidator.validateMultipleAuthErrors(response.text)

    }

    @Test
    fun `gets 404 if resource file doesnt exist`()
    {
        insertReport("testname", "testversion", hashResources = "{\"resource.csv\": \"gfe7064mvdfjieync\"}")

        val url = "/reports/testname/testversion/resources/resource.csv/"
        val token = requestHelper.generateOnetimeToken(url)
        val response = requestHelper.get("$url?access_token=$token", ContentTypes.binarydata)

        assertJsonContentType(response)
        Assertions.assertThat(response.statusCode).isEqualTo(404)
        JSONValidator.validateError(response.text, "file-not-found", "File with name 'resource.csv' does not exist")
    }

}
