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
        val response = requestHelper.get("/reports/testname/testversion/resources/$fakeresource", ContentTypes.binarydata)

        assertJsonContentType(response)
        Assertions.assertThat(response.statusCode).isEqualTo(404)
        JSONValidator.validateError(response.text, "unknown-resource", "Unknown resource : '$fakeresource'")
    }

}
