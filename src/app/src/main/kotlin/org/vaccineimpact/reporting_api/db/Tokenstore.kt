package org.vaccineimpact.reporting_api.db

import org.jooq.impl.DSL.*
import org.jooq.impl.SQLDataType
import org.slf4j.LoggerFactory
import java.io.File
import java.sql.DriverManager
import java.sql.SQLException

class TokenStore
{
    val tableName = "ONETIME_TOKEN"
    val ONETIME_TOKEN = table(name(tableName))
    val TOKEN = field(name("$tableName.TOKEN"))

    private val logger = LoggerFactory.getLogger(TokenStore::class.java)

    private fun getJooqContext() = JooqContext(Config["onetime_token.db.location"])

    fun setup()
    {

        val dbLocation = Config["onetime_token.db.location"]
        val file = File(dbLocation)

        if (file.exists())
        {
            file.delete()
        }

        val url = "jdbc:sqlite:${Config["onetime_token.db.location"]}"

        try
        {

            DriverManager.getConnection(url).use { conn ->
                if (conn != null)
                {
                    logger.info("A new database has been created at ${Config["onetime_token.db.location"]}.")
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

    fun storeToken(token: String)
    {
        getJooqContext().use {
            it.dsl.insertInto(ONETIME_TOKEN)
                    .set(TOKEN, token)
                    .execute()
        }
    }

    fun validateOneTimeToken(token: String): Boolean
    {
        getJooqContext().use {
            val deletedCount = it.dsl.deleteFrom(ONETIME_TOKEN)
                    .where(TOKEN.eq(token))
                    .execute()

            return deletedCount == 1
        }
    }
}
