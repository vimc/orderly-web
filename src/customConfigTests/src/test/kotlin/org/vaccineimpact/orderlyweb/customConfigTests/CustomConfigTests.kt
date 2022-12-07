package org.vaccineimpact.orderlyweb.customConfigTests

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.vaccineimpact.orderlyweb.app_start.main
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.Tables.ORDERLYWEB_REPORT_VERSION_FULL
import org.vaccineimpact.orderlyweb.db.getResource
import org.vaccineimpact.orderlyweb.test_helpers.http.Response
import java.io.File
import java.net.BindException
import java.net.ServerSocket


abstract class CustomConfigTests
{
    fun startApp(customConfig: String)
    {
        AppConfig.properties.apply {
            customConfig.byteInputStream().use { load(it) }
        }

        while (!isPortAvailable())
        {
            Thread.sleep(500)
            System.err.println("Port not available yet")
        }
        while (isSparkInstanceRunning())
        {
            Thread.sleep(500)
            System.err.println("Spark not available yet")
        }

        main(emptyArray())
        Thread.sleep(500)
    }

    @BeforeEach
    fun createDatabase()
    {
        val db = AppConfig()["db.location"]
        while (!isDBAvailable(db))
        {
            Thread.sleep(500)
            System.err.println("db not available yet at $db")
        }
    }

    @AfterEach
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
                .withFailMessage(response.text)
                .isEqualTo(200)
    }

    protected fun assertHtmlContentType(response: Response)
    {
        Assertions.assertThat(response.headers["content-type"]).isEqualTo("text/html")
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

    private fun isSparkInstanceRunning(): Boolean
    {
        return try
        {
            return spark.Spark.routes().size > 0
        }
        catch (e: Exception)
        {
            false
        }
    }

    private fun isDBAvailable(dbLocation: String): Boolean
    {
        return try
        {
            JooqContext(dbLocation).use {
                it.dsl.selectFrom(ORDERLYWEB_REPORT_VERSION_FULL).fetchAny()
            }
            true
        }
        catch (e: Exception)
        {
            false
        }
    }
}