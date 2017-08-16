package org.vaccineimpact.reporting_api.tests.integration_tests.tests

import khttp.responses.Response
import org.assertj.core.api.Assertions
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.vaccineimpact.reporting_api.app_start.main
import org.vaccineimpact.reporting_api.db.Config
import org.vaccineimpact.reporting_api.test_helpers.MontaguTests
import org.vaccineimpact.reporting_api.tests.integration_tests.APITests
import org.vaccineimpact.reporting_api.tests.integration_tests.helpers.RequestHelper
import org.vaccineimpact.reporting_api.tests.integration_tests.validators.JSONValidator
import spark.Spark
import java.io.File

abstract class IntegrationTest : MontaguTests()
{
    val requestHelper = RequestHelper()
    val JSONValidator = JSONValidator()

    companion object
    {

        @BeforeClass @JvmStatic
        fun startApp()
        {
            if (!APITests.appStarted)
            {
                main(emptyArray())
            }
        }

        @AfterClass @JvmStatic
        fun stopApp()
        {

            if (!APITests.appStarted)
            {
                Spark.stop()
                File(Config["onetime_token.db.location"]).delete()
            }
        }
    }

    @Before
    fun createDatabase()
    {

        println("Copying database from: ${Config["db.template"]}")

        val newDbFile = File(Config["db.location"])
        val source = File(Config["db.template"])

        source.copyTo(newDbFile, true)
    }

    @After
    fun deleteDatabases()
    {
        File(Config["db.location"]).delete()
    }

    protected fun assertSuccessful(response: Response)
    {
        Assertions.assertThat(response.statusCode).isEqualTo(200)
        Assertions.assertThat(response.headers["Content-Encoding"]).isEqualTo("gzip")
    }

    protected fun assertJsonContentType(response: Response)
    {
        Assertions.assertThat(response.headers["content-type"]).isEqualTo("application/json; charset=utf-8")
    }

}