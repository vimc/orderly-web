package org.vaccineimpact.reporting_api.tests.integration_tests.tests

import khttp.responses.Response
import org.assertj.core.api.Assertions
import org.junit.After
import org.junit.Before
import org.vaccineimpact.reporting_api.db.Config
import org.vaccineimpact.reporting_api.test_helpers.MontaguTests
import org.vaccineimpact.reporting_api.tests.integration_tests.helpers.RequestHelper
import org.vaccineimpact.reporting_api.tests.integration_tests.validators.JSONValidator
import java.io.File

abstract class IntegrationTest: MontaguTests()
{
    val requestHelper = RequestHelper()
    val JSONValidator = JSONValidator()

    @Before
    fun createDatabase(){

        println("Copying database from: ${Config["db.template"]}")

        val newDbFile = File(Config["db.location"])
        val source = File(Config["db.template"])

        source.copyTo(newDbFile, true)
    }

    @After
    fun deleteDatabase(){
        File(Config["db.location"]).delete()
    }

    protected fun assertSuccessful(response: Response){
        Assertions.assertThat(response.statusCode).isEqualTo(200)
    }

    protected fun assertJsonContentType(response: Response){
        Assertions.assertThat(response.headers["content-type"]).isEqualTo("application/json; charset=utf-8")
    }

}