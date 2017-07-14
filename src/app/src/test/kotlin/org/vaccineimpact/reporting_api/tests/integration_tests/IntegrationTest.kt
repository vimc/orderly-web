package org.vaccineimpact.reporting_api.tests.integration_tests

import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.vaccineimpact.reporting_api.app_start.main
import org.vaccineimpact.reporting_api.db.Config
import org.vaccineimpact.reporting_api.test_helpers.MontaguTests
import spark.Spark
import java.io.File

abstract class IntegrationTest: MontaguTests()
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

    @Before
    fun createDatabase(){

        println("Looking for sqlite database at path: ${Config["db.template"]}")
        println("Working directory: ${System.getProperty("user.dir")}")

        val newDbFile = File(Config["db.location"])
        val source = File(Config["db.template"])

        source.copyTo(newDbFile, true)
    }

    @After
    fun deleteDatabase(){
        File(Config["db.location"]).delete()
    }

}