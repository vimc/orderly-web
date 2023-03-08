package org.vaccineimpact.orderlyweb.customConfigTests

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInfo
import org.vaccineimpact.orderlyweb.app_start.main
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.Tables.ORDERLYWEB_REPORT_VERSION_FULL
import org.vaccineimpact.orderlyweb.db.getResource
import org.vaccineimpact.orderlyweb.test_helpers.http.HttpClient
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

    private fun createDatabase()
    {
        val db = AppConfig()["db.location"]
        while (!isDBAvailable(db))
        {
            Thread.sleep(500)
            System.err.println("db not available yet at $db")
        }
    }

    private fun changeConfig(defaultBranchOnly: Boolean)
    {
        val mapper = ObjectMapper(YAMLFactory())
        val configFile = File("${AppConfig()["orderly.root"]}/orderly_config.yml")
        val oldConfig = mapper.readValue(configFile, OrderlyConfig::class.java)

        if (oldConfig.remote!!["main"]?.default_branch_only != defaultBranchOnly)
        {
            val newRemote = oldConfig.remote!!["main"]!!.copy(default_branch_only = defaultBranchOnly)
            val newConfig = oldConfig.copy(remote = linkedMapOf("main" to newRemote))
            configFile.writeText(mapper.writeValueAsString(newConfig))
        }
        val response = HttpClient.post("http://localhost:8321/v1/reload/")
        if (response.statusCode != 200)
        {
            throw Exception("Failed to change orderly config")
        }
    }


    // We could probably do something fiddly with reflection to get the enum for parameterized tests
    // here, but it would be fairly complicated, so for now just relying on the displayName which will
    // contain the enum value
    @BeforeEach
    fun beforeEach(info: TestInfo)
    {
        createDatabase()
        val defaultBranchOnly = info.displayName.contains("DEFAULT_BRANCH_ONLY")
        changeConfig(defaultBranchOnly)
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