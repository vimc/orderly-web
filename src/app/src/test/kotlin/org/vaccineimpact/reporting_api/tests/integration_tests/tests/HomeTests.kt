package org.vaccineimpact.reporting_api.tests.integration_tests.tests

import org.junit.Test
import org.vaccineimpact.reporting_api.tests.integration_tests.helpers.RequestHelper

class HomeTests : IntegrationTest()
{
    @Test
    fun `can get index page`()
    {
        val response = RequestHelper().getNoAuth("/")
        assertSuccessful(response)
    }
}