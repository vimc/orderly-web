package org.vaccineimpact.reporting_api.tests.integration_tests

import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.runner.RunWith
import org.junit.runners.Suite
import org.vaccineimpact.reporting_api.app_start.main
import org.vaccineimpact.reporting_api.db.AppConfig
import org.vaccineimpact.reporting_api.tests.integration_tests.helpers.TestTokenGenerator
import org.vaccineimpact.reporting_api.tests.integration_tests.tests.*
import spark.Spark
import java.io.File

@RunWith(Suite::class)
@Suite.SuiteClasses(ArtefactTests::class,
        ReportTests::class,
        ResourceTests::class,
        DataTests::class,
        SecurityTests::class,
        OnetimeTokenTests::class,
        GitTests:: class)
class APITests
{
    companion object
    {
        // Use a single TestTokenGenerator for the whole suite. This
        // ensures that the same keypair is used throughout.
        val tokenHelper = TestTokenGenerator()

        @BeforeClass @JvmStatic
        fun startApp()
        {
            appStarted = true
            main(emptyArray())
        }

        @AfterClass @JvmStatic
        fun stopApp()
        {
            Spark.stop()
            File(AppConfig()["onetime_token.db.location"]).delete()
        }

        @JvmStatic
        var appStarted = false
    }
}