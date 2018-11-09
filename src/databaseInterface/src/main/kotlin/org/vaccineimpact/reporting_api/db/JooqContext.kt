package org.vaccineimpact.reporting_api.db

import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import java.io.File
import java.sql.Connection
import java.sql.DriverManager

open class JooqContext(private val dbLocation: String = AppConfig()["db.location"]) : AutoCloseable
{
    private val conn = getConnection()
    val dsl = createDSL(conn)

    private fun getConnection(): Connection
    {
        println("Making DB connection at $dbLocation")

        val fullLocation = File(dbLocation).absolutePath
        val url = "jdbc:sqlite://$fullLocation"
        try
        {
            return DriverManager.getConnection(url)
        }
        catch (e: Exception)
        {
            throw UnableToConnectToDatabase(url)
        }
    }

    private fun createDSL(conn: Connection): DSLContext
    {
        return DSL.using(conn, SQLDialect.SQLITE)
    }

    override fun close()
    {
        conn.close()
    }
}
