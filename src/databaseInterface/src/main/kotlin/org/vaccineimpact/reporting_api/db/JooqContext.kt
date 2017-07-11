package org.vaccineimpact.reporting_api.db

import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import java.sql.Connection
import java.sql.DriverManager

open class JooqContext(val dbLocation: String? = null) : AutoCloseable
{
    private val conn = getConnection()
    val dsl = createDSL(conn)

    private fun getConnection(): Connection
    {
        val dbLocation = dbLocation ?:Config["db.location"]
        val url = "jdbc:sqlite://$dbLocation"
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
