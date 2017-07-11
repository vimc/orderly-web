package org.vaccineimpact.reporting_api.test_helpers

import org.junit.After
import org.junit.Before
import org.junit.BeforeClass
import org.vaccineimpact.reporting_api.db.Config
import org.vaccineimpact.reporting_api.db.JooqContext
import org.vaccineimpact.reporting_api.db.UnableToConnectToDatabase

abstract class DatabaseTest : MontaguTests()
{

    @Before
    fun createDatabase()
    {
        JooqContext().use {
            it.dsl.query("CREATE DATABASE $dbName TEMPLATE $templateDbName;").execute()
        }
        DatabaseChecker.checkDatabaseExists(dbName)
    }

    @After
    fun dropDatabase()
    {
        org.vaccineimpact.reporting_api.db.JooqContext().use {
            it.dsl.query("DROP DATABASE $dbName").execute()
        }
    }

    companion object
    {
        private val templateDbName = Config["testdb.template_name"]
        private val dbName = Config["db.name"]

        @BeforeClass @JvmStatic
        fun setupTestEnvironment()
        {
            if (!DatabaseChecker.check(templateDbName))
            {
                println("Template database does not exist, will rename from '${dbName}'")
                DatabaseChecker.checkDatabaseExists(dbName)
                JooqContext().use {
                    it.dsl.query("ALTER DATABASE ${dbName} RENAME TO ${templateDbName}").execute()
                }
                println("Created template database by renaming ${dbName} to ${templateDbName}")
                DatabaseChecker.checkDatabaseExists(templateDbName)

            }
        }
    }
}

object DatabaseChecker
{
    private var error: Exception? = null

    fun checkDatabaseExists(dbName: String): Unit
    {
        if (!databaseExists(dbName))
        {
            throw error!!
        }
    }

    fun databaseExists(dbName: String): Boolean
    {
        println("Checking that database '$dbName' exists...")
        var attemptsRemaining = 10
        while (attemptsRemaining > 0)
        {
            if (check(dbName))
            {
                return true
            }
            else
            {
                println("Unable to connect. I will wait and then retry $attemptsRemaining more times")
                attemptsRemaining--
                Thread.sleep(2000)
            }
        }
        return false
    }

    fun check(dbName: String): Boolean
    {
        try
        {
            JooqContext().close()
            return true
        }
        catch (e: UnableToConnectToDatabase)
        {
            error = e
            return false
        }
    }
}