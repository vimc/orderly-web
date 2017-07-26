package org.vaccineimpact.reporting_api.tests.integration_tests.tests

import org.assertj.core.api.Assertions
import org.junit.Test
import org.vaccineimpact.reporting_api.ContentTypes
import org.vaccineimpact.reporting_api.tests.insertReport

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
    fun `gets 404 if resource doesnt exist in db`()
    {
        insertReport("testname", "testversion")
        val fakeresource = "hf647rhj"
        val token = requestHelper.generateOnetimeToken()
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
        val token = requestHelper.generateOnetimeToken()
        val response = requestHelper.get("/reports/testname/testversion/resources/resource.csv/?access_token=$token", ContentTypes.binarydata)

        assertJsonContentType(response)
        Assertions.assertThat(response.statusCode).isEqualTo(404)
        JSONValidator.validateError(response.text, "file-not-found", "File with name 'resource.csv' does not exist")
    }

}
