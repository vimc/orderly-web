package org.vaccineimpact.orderlyweb.tests.database_tests

import org.junit.After
import org.junit.Before
import org.vaccineimpact.orderlyweb.db.AppConfig
import java.io.File

abstract class CleanDatabaseTests: DatabaseTests()
{

    @Before
    fun createDatabase()
    {
        println("Looking for sqlite database at path: ${AppConfig()["db.template"]}")
        println("Working directory: ${System.getProperty("user.dir")}")

        val newDbFile = File(AppConfig()["db.location"])
        val source = File(AppConfig()["db.template"])

        source.copyTo(newDbFile, true)
    }

    @After
    fun dropDatabase()
    {
        File(AppConfig()["db.location"]).delete()
    }

}