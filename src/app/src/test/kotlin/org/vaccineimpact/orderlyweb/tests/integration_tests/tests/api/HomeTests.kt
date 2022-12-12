package org.vaccineimpact.orderlyweb.tests.integration_tests.tests.api

import org.junit.jupiter.api.Test
import org.vaccineimpact.orderlyweb.tests.integration_tests.helpers.APIRequestHelper
import org.vaccineimpact.orderlyweb.tests.integration_tests.tests.IntegrationTest

class HomeTests : IntegrationTest()
{
    @Test
    fun `can get index page`()
    {
        val response = APIRequestHelper().getNoAuth("/")
        assertSuccessful(response)
    }
}