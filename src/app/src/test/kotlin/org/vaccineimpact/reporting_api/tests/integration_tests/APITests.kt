package org.vaccineimpact.reporting_api.tests.integration_tests

import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.runner.RunWith
import org.junit.runners.Suite
import org.vaccineimpact.reporting_api.app_start.main
import org.vaccineimpact.reporting_api.tests.integration_tests.tests.*
import spark.Spark

@RunWith(Suite::class)
@Suite.SuiteClasses(ArtefactTests::class,
        ReportTests::class,
        ResourceTests::class,
        DataTests::class,
        SecurityTests::class)
class APITests
{
    companion object {

        @BeforeClass @JvmStatic
        fun startApp() {
            appStarted = true
            main(emptyArray())
        }

        @AfterClass @JvmStatic
        fun stopApp(){

            Spark.stop()
        }

        @JvmStatic
        var appStarted = false

    }

}