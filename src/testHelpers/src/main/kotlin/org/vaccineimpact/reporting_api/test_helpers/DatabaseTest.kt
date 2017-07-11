package org.vaccineimpact.reporting_api.test_helpers

import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.vaccineimpact.reporting_api.db.Config
import org.vaccineimpact.reporting_api.db.JooqContext
import org.vaccineimpact.reporting_api.db.Tables.*
import java.io.File

abstract class DatabaseTest
{
    companion object
    {

        @BeforeClass @JvmStatic
        fun createDatabase()
        {
            val newDbFile = File(Config["dbTest.location"])
            val source = File("/${Config["db.location"]}")

            source.copyTo(newDbFile, true)
        }

        @AfterClass @JvmStatic
        fun dropDatabase()
        {
            File(Config["dbTest.location"]).delete()
        }
    }

    @Before
    fun clearDatabase()
    {
        JooqContext(File(Config["dbTest.location"]).absolutePath).use{
            it.dsl.deleteFrom(ORDERLY)
                    .execute()
        }

    }

}