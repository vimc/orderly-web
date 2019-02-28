package org.vaccineimpact.orderlyweb.appstart

import org.slf4j.LoggerFactory
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.TokenStore
import org.vaccineimpact.orderlyweb.security.AllowedOriginsFilter
import java.net.BindException
import java.net.ServerSocket
import kotlin.system.exitProcess
import spark.Spark as spk

fun main(args: Array<String>)
{
    val api = MontaguReportingApi()
    api.run()
}

class MontaguReportingApi
{
    private val urlBase = "/v1"

    private val logger = LoggerFactory.getLogger(MontaguReportingApi::class.java)

    fun run()
    {
        waitForGoSignal()
        setupPort()
        spk.redirect.get("/", urlBase)
        spk.before("*", AllowedOriginsFilter(AppConfig().getBool("allow.localhost")))
        spk.options("*") { _, res ->
            res.header("Access-Control-Allow-Headers", "Authorization")
        }

        logger.info("Expecting orderly database at ${AppConfig()["db.location"]}")

        TokenStore.instance.setup()
        ErrorHandler.setup()
        Router(ApiRouteConfig).mapEndpoints(urlBase)
        Router(WebRouteConfig).mapEndpoints("")

        if (!AppConfig().authEnabled)
        {
            logger.warn("WARNING: AUTHENTICATION IS DISABLED")
        }
    }

    private fun setupPort()
    {
        val config = AppConfig()
        val port = if (config.authEnabled)
        {
            config.getInt("app.port")
        }
        else
        {
            8888
        }
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
