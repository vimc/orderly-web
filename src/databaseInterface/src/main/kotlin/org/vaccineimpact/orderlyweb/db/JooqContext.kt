package org.vaccineimpact.orderlyweb.db

import com.sun.org.apache.xpath.internal.operations.Bool
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import java.io.File
import java.sql.Connection
import java.sql.DriverManager

open class JooqContext(private val dbLocation: String = AppConfig()["db.location"],
                       private val enableForeignKeyConstraints: Boolean = true) : AutoCloseable
{
    private val conn = getConnection()
    val dsl = createDSL(conn)

    private fun getConnection(): Connection
    {
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
        if (enableForeignKeyConstraints)
        {
            conn.prepareStatement("PRAGMA foreign_keys = ON;")
                    .execute()
        }

        return DSL.using(conn, SQLDialect.SQLITE)
    }

    override fun close()
    {
        conn.close()
    }
}
