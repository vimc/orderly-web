package org.vaccineimpact.orderlyweb.tests.integration_tests.tests.api

import org.junit.Test
import org.vaccineimpact.orderlyweb.tests.integration_tests.helpers.RequestHelper
import org.vaccineimpact.orderlyweb.tests.integration_tests.tests.IntegrationTest

class HomeTests : IntegrationTest()
{
    @Test
    fun `can get index page`()
    {
        val response = RequestHelper().getNoAuth("/")
        assertSuccessful(response)
    }
}