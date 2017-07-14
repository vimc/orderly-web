package org.vaccineimpact.reporting_api.tests.integration_tests

import org.junit.AfterClass
import org.junit.BeforeClass
import org.vaccineimpact.reporting_api.app_start.main
import spark.Spark

class ReportTests
{
    companion object {

        @BeforeClass @JvmStatic
        fun StartApp() {
            main(emptyArray())
        }

        @AfterClass @JvmStatic
        fun StopApp(){
            Spark.stop()
        }

    }

    fun `can get reports`()
    {
    }
}
