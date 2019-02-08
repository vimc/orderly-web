package org.vaccineimpact.reporting_api.tests.integration_tests.tests

import org.assertj.core.api.Assertions
import org.junit.Test
import org.vaccineimpact.api.models.FilePurpose
import org.vaccineimpact.reporting_api.ContentTypes
import org.vaccineimpact.reporting_api.db.AppConfig
import org.vaccineimpact.reporting_api.tests.insertFileInput
import org.vaccineimpact.reporting_api.tests.insertReport
import java.io.File

class ResourceTests : IntegrationTest()
{
    @Test
    fun `gets dict of resource names to hashes with scoped report reading permission`()
    {
        insertReport("testname", "testversion")
        val response = requestHelper.get("/reports/testname/versions/testversion/resources",
                user = fakeReportReader("testname"))

        assertJsonContentType(response)
    }

    @Test
    fun `can't get dict of resource names to hashes if report not in scoped permissions`()
    {
        insertReport("testname", "testversion")
        val response = requestHelper.get("/reports/testname/versions/testversion/resources",
                user = fakeReportReader("testname"))

        assertJsonContentType(response)
    }

    @Test
    fun `gets resource file`()
    {
        val version = File("${AppConfig()["orderly.root"]}/archive/use_resource/").list()[0]

        val resourceEncoded = "meta:data.csv"
        val url = "/reports/use_resource/versions/$version/resources/$resourceEncoded/"
        val token = requestHelper.generateOnetimeToken(url)
        val response = requestHelper.get("$url?access_token=$token", ContentTypes.binarydata)

        assertSuccessful(response)
        Assertions.assertThat(response.headers["content-type"]).isEqualTo("application/octet-stream")
        Assertions.assertThat(response.headers["content-disposition"]).isEqualTo("attachment; filename=use_resource/$version/meta/data.csv")

    }

    @Test
    fun `can't get resource file if report not in scoped permissions`()
    {
        val version = File("${AppConfig()["orderly.root"]}/archive/use_resource/").list()[0]

        val resourceEncoded = "meta:data.csv"
        val url = "/reports/use_resource/versions/$version/resources/$resourceEncoded/"
        val token = requestHelper.generateOnetimeToken(url)
        val response = requestHelper.get("$url?access_token=$token", ContentTypes.binarydata,
                user = fakeReportReader("badereportname"))

        assertUnauthorized(response, "use_resource")
    }

    @Test
    fun `gets 404 if resource doesnt exist in db`()
    {
        insertReport("testname", "testversion")
        val fakeresource = "hf647rhj"
        val url = "/reports/testname/versions/testversion/resources/$fakeresource/"
        val token = requestHelper.generateOnetimeToken(url)
        val response = requestHelper.get("/reports/testname/versions/testversion/resources/$fakeresource/?access_token=$token", ContentTypes.binarydata)

        assertJsonContentType(response)
        Assertions.assertThat(response.statusCode).isEqualTo(404)
        JSONValidator.validateError(response.text, "unknown-resource", "Unknown resource : '$fakeresource'")
    }

    @Test
    fun `gets 401 if missing access token`()
    {
        insertReport("testname", "testversion")
        val fakeresource = "hf647rhj"
        val response = requestHelper.getNoAuth("/reports/testname/versions/testversion/resources/$fakeresource/", ContentTypes.binarydata)

        Assertions.assertThat(response.statusCode).isEqualTo(401)
        JSONValidator.validateMultipleAuthErrors(response.text)

    }

    @Test
    fun `gets 404 if resource file doesnt exist`()
    {
        insertReport("testname", "testversion")
        insertFileInput("testversion", "resource.csv", FilePurpose.RESOURCE)

        val url = "/reports/testname/versions/testversion/resources/resource.csv/"
        val token = requestHelper.generateOnetimeToken(url)
        val response = requestHelper.get("$url?access_token=$token", ContentTypes.binarydata)

        assertJsonContentType(response)
        Assertions.assertThat(response.statusCode).isEqualTo(404)
        JSONValidator.validateError(response.text, "file-not-found", "File with name 'resource.csv' does not exist")
    }

}
