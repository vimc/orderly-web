package org.vaccineimpact.orderlyweb.app_start

import freemarker.template.Configuration
import org.slf4j.LoggerFactory
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.TokenStore
import org.vaccineimpact.orderlyweb.security.AllowedOriginsFilter
import org.vaccineimpact.orderlyweb.security.authentication.AuthenticationConfig
import spark.Spark.staticFiles
import spark.template.freemarker.FreeMarkerEngine
import java.io.File
import java.net.BindException
import java.net.ServerSocket
import kotlin.system.exitProcess
import spark.Spark as spk
import java.io.PrintWriter
import java.io.StringWriter
import java.util.*


fun main(args: Array<String>)
{
    val app = OrderlyWeb()
    app.run()
}

fun buildFreemarkerConfig(templateDirectory: File): Configuration
{
    val freeMarkerConfig = Configuration(Configuration.VERSION_2_3_26)
    freeMarkerConfig.defaultEncoding = "UTF-8"
    freeMarkerConfig.locale = Locale.UK
    freeMarkerConfig.objectWrapper = TemplateObjectWrapper()
    freeMarkerConfig.setDirectoryForTemplateLoading(templateDirectory)
    freeMarkerConfig.addAutoInclude("layouts/layout.ftl")
    freeMarkerConfig.addAutoInclude("layouts/layoutwide.ftl")

    return freeMarkerConfig
}

class OrderlyWeb
{
    private val logger = LoggerFactory.getLogger(OrderlyWeb::class.java)

    private fun getPropertiesAsString(prop: Properties): String
    {
        val writer = StringWriter()
        prop.list(PrintWriter(writer))
        return writer.buffer.toString()
    }

    fun run()
    {
        val freeMarkerConfig = buildFreemarkerConfig(File("templates").absoluteFile)

        staticFiles.externalLocation(File("static/public").absolutePath)

        waitForGoSignal()

        logger.info("Using the following config")
        logger.info(getPropertiesAsString(AppConfig.properties))

        setupPort()
        spk.before("*", AllowedOriginsFilter(AppConfig().getBool("allow.localhost")))
        spk.options("*") { _, res ->
            res.header("Access-Control-Allow-Headers", "Authorization")
        }

        logger.info("Expecting orderly database at ${AppConfig()["db.location"]}")

        TokenStore.instance.setup()

        val router = Router(freeMarkerConfig)
        router.mapEndpoints(APIRouteConfig, Router.apiUrlBase)
        router.mapEndpoints(WebRouteConfig, "")
    }

    private fun setupPort()
    {
        val config = AppConfig()
        val port = config.getInt("app.port")

        var attempts = 5
        spk.port(port)

        while (!isPortAvailable(port) && attempts > 0)
        {
            logger.info("Waiting for port $port to be available, $attempts attempts remaining")
            Thread.sleep(2000)
            attempts--
        }
        if (attempts == 0)
        {
            logger.error("Unable to bind to port $port - it is already in use.")
            exitProcess(-1)
        }
    }

    private fun isPortAvailable(port: Int): Boolean
    {
        try
        {
            ServerSocket(port).use {}
            return true
        }
        catch (e: BindException)
        {
            return false
        }
    }
}
