package org.vaccineimpact.reporting_api.tests.integration_tests

import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.runner.RunWith
import org.junit.runners.Suite
import org.vaccineimpact.reporting_api.app_start.main
import org.vaccineimpact.reporting_api.tests.integration_tests.tests.ArtefactTests
import org.vaccineimpact.reporting_api.tests.integration_tests.tests.ReportTests
import spark.Spark

@RunWith(Suite::class)
@Suite.SuiteClasses(ArtefactTests::class, ReportTests::class)
class APITests
{
    companion object {

        @BeforeClass @JvmStatic
        fun startApp() {

            main(emptyArray())
        }

        @AfterClass @JvmStatic
        fun stopApp(){

            Spark.stop()
        }
    }

}