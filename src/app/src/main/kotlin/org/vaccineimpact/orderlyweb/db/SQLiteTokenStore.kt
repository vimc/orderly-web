package org.vaccineimpact.orderlyweb.db

import org.jooq.impl.DSL.*
import org.jooq.impl.SQLDataType
import org.slf4j.LoggerFactory
import java.io.File
import java.sql.DriverManager
import java.sql.SQLException

class SQLiteTokenStore(private val config: Config = AppConfig()) : OnetimeTokenStore
{
    companion object {
        const val TABLE_NAME = "ONETIME_TOKEN"
    }

    val oneTimeToken = table(name(TABLE_NAME))
    val token = field(name("$TABLE_NAME.TOKEN"))

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
            it.dsl.createTable(TABLE_NAME)
                    .column("$TABLE_NAME.TOKEN", SQLDataType.VARCHAR)
                    .execute()
        }
    }

    override fun storeToken(token: String)
    {
        getJooqContext().use {
            it.dsl.insertInto(oneTimeToken)
                    .set(this.token, token)
                    .execute()
        }
    }

    override fun validateOneTimeToken(token: String): Boolean
    {
        getJooqContext().use {
            val deletedCount = it.dsl.deleteFrom(oneTimeToken)
                    .where(this.token.eq(token))
                    .execute()

            return deletedCount == 1
        }
    }
}
