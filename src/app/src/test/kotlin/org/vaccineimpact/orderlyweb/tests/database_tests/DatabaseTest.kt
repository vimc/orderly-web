package org.vaccineimpact.orderlyweb.tests.database_tests

import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests
import java.io.File

abstract class DatabaseTests : TeamcityTests()
{
    @Before
    open fun setup()
    {
        println("Looking for sqlite database at path: ${AppConfig()["db.template"]}")
        println("Working directory: ${System.getProperty("user.dir")}")

        val newDbFile = File(AppConfig()["db.location"])
        val source = File(AppConfig()["db.template"])

        source.copyTo(newDbFile, true)
    }

    @After
    fun teardown()
    {
        File(AppConfig()["db.location"]).delete()
    }

}