package org.vaccineimpact.reporting_api.db

import org.jooq.impl.DSL.*
import org.jooq.impl.SQLDataType
import org.slf4j.LoggerFactory
import org.vaccineimpact.reporting_api.security.OnetimeTokenStore
import java.io.File
import java.sql.DriverManager
import java.sql.SQLException

class SQLiteTokenStore(private val config: Config = AppConfig()) : OnetimeTokenStore
{
    val tableName = "ONETIME_TOKEN"
    val ONETIME_TOKEN = table(name(tableName))
    val TOKEN = field(name("$tableName.TOKEN"))

    private val logger = LoggerFactory.getLogger(SQLiteTokenStore::class.java)

    private fun getJooqContext() = JooqContext(this.config["onetime_token.db.location"])

    override fun setup()
    {

        val dbLocation = this.config["onetime_token.db.location"]
        val file = File(dbLocation)

        if (file.exists())
        {
            file.delete()
        }

        val url = "jdbc:sqlite:${this.config["onetime_token.db.location"]}"

        try
        {

            DriverManager.getConnection(url).use { conn ->
                if (conn != null)
                {
                    logger.info("A new database has been created at ${this.config["onetime_token.db.location"]}.")
                }
            }

            createTable()

        }
        catch (e: SQLException)
        {
            logger.error(e.message)
        }
    }

    private fun createTable()
    {
        getJooqContext().use {
            it.dsl.createTable(tableName)
                    .column("$tableName.TOKEN", SQLDataType.VARCHAR)
                    .execute()
        }
    }

    override fun storeToken(uncompressedToken: String)
    {
        getJooqContext().use {
            it.dsl.insertInto(ONETIME_TOKEN)
                    .set(TOKEN, uncompressedToken)
                    .execute()
        }
    }

    override fun validateOneTimeToken(uncompressedToken: String): Boolean
    {
        getJooqContext().use {
            val deletedCount = it.dsl.deleteFrom(ONETIME_TOKEN)
                    .where(TOKEN.eq(uncompressedToken))
                    .execute()

            return deletedCount == 1
        }
    }
}
