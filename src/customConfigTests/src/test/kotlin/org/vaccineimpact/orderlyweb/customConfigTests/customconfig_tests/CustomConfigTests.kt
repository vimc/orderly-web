package org.vaccineimpact.orderlyweb.customConfigTests.customconfig_tests

import khttp.responses.Response
import org.assertj.core.api.Assertions
import org.junit.After
import org.junit.Before
import org.vaccineimpact.orderlyweb.app_start.main
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.getResource
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests
import java.io.File
import java.net.BindException
import java.net.ServerSocket

abstract class CustomConfigTests : TeamcityTests()
{
    fun startApp(customConfig: String)
    {
        AppConfig.properties.apply {
            customConfig.byteInputStream().use { load(it) }
        }

        while (!isPortAvailable())
        {
            Thread.sleep(500)
        }

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
        spark.Spark.stop()
        File(AppConfig()["db.location"]).delete()

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

    private fun isPortAvailable(): Boolean
    {
        return try
        {
            ServerSocket(AppConfig().getInt("app.port")).use {}
            true
        }
        catch (e: BindException)
        {
            false
        }
    }
}