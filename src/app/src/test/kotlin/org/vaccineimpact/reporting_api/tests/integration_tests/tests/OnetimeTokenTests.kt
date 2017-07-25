package org.vaccineimpact.reporting_api.tests.integration_tests.tests

import org.junit.Test
import org.vaccineimpact.reporting_api.tests.insertReport

class OnetimeTokenTests : IntegrationTest()
{

    @Test
    fun `gets one time token`()
    {

        insertReport("testname", "testversion")
        val response = requestHelper.get("/access_token/")

        assertJsonContentType(response)
        assertSuccessful(response)
        JSONValidator.validateAgainstSchema(response.text, "Token")

    }

}