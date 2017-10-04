package org.vaccineimpact.reporting_api.tests.integration_tests.tests

import org.junit.Test
import org.vaccineimpact.reporting_api.OrderlyServer
import org.vaccineimpact.reporting_api.db.Config

class OrderlyServerTests
{
    @Test
    fun `can run reports`()
    {

        val sut = OrderlyServer(Config)
      //  val response = sut.post("/reports/minimal/run/")
    }

}