package org.vaccineimpact.reporting_api.app_start

import org.slf4j.LoggerFactory
import org.vaccineimpact.reporting_api.db.Config
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
        setupPort()
        spk.redirect.get("/", urlBase)
        spk.before("*", ::addTrailingSlashes)

        logger.info("Expecting orderly database at ${Config["db.location"]}")

        ErrorHandler.setup()
        Router(MontaguRouteConfig).mapEndpoints(urlBase)
    }

    private fun setupPort()
    {
        val port = Config.getInt("app.port")
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
