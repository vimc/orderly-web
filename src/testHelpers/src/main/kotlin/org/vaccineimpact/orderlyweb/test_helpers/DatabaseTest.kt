package org.vaccineimpact.orderlyweb.test_helpers

import org.junit.AfterClass
import org.junit.BeforeClass
import org.vaccineimpact.orderlyweb.db.AppConfig
import java.io.File

abstract class DatabaseTests
{

    companion object
    {

        @BeforeClass
        @JvmStatic
        fun createDatabase()
        {
            println("Looking for sqlite database at path: ${AppConfig()["db.template"]}")
            println("Working directory: ${System.getProperty("user.dir")}")

            val newDbFile = File(AppConfig()["db.location"])
            val source = File(AppConfig()["db.template"])

            source.copyTo(newDbFile, true)
        }

        @AfterClass
        @JvmStatic
        fun dropDatabase()
        {
            File(AppConfig()["db.location"]).delete()
        }

    }


}