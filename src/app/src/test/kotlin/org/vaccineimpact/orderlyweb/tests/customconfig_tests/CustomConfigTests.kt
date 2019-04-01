package org.vaccineimpact.orderlyweb.tests.customconfig_tests

import khttp.responses.Response
import org.assertj.core.api.Assertions
import org.junit.After
import org.junit.Before
import org.vaccineimpact.orderlyweb.app_start.main
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.getResource
import org.vaccineimpact.orderlyweb.test_helpers.MontaguTests
import java.io.File

abstract class CustomConfigTests : MontaguTests()
{
    var appRunning: Boolean = false

    fun startApp(customConfig: String)
    {
        spark.Spark.stop()
        val localConfig = File("local")
        localConfig.createNewFile()
        localConfig.writeText(customConfig)

        AppConfig.properties.apply {
            localConfig.inputStream().use { load(it) }
        }

        while (appRunning)
        {
            Thread.sleep(500)
        }
        appRunning = true
        main(emptyArray())
        Thread.sleep(500)
    }

    @Before
    fun createDatabase()
    {
        println("Copying database from: ${AppConfig()["db.template"]}")

        val newDbFile = File(AppConfig()["db.location"])
        val source = File(AppConfig()["db.template"])

        source.copyTo(newDbFile, true)
        Thread.sleep(1000)
    }

    @After
    fun cleanup()
    {
        File(AppConfig()["db.location"]).delete()
        File("local").delete()

        // reset the properties
        AppConfig.properties.apply {
            load(getResource("config.properties").openStream())
            val global = File("/etc/orderly/web/config.properties")
            if (global.exists())
            {
                global.inputStream().use { load(it) }
            }
        }
    }

    protected fun assertSuccessful(response: Response)
    {
        Assertions.assertThat(response.statusCode)
                .isEqualTo(200)

        Assertions.assertThat(response.headers["Content-Encoding"]).isEqualTo("gzip")
    }

}