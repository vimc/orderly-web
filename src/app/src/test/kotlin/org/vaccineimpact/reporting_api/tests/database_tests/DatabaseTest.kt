package org.vaccineimpact.reporting_api.tests.database_tests

import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.vaccineimpact.reporting_api.db.Config
import org.vaccineimpact.reporting_api.db.JooqContext
import org.vaccineimpact.reporting_api.db.Tables.ORDERLY
import org.vaccineimpact.reporting_api.test_helpers.MontaguTests
import java.io.File

abstract class DatabaseTests : MontaguTests() {
    companion object {

        @BeforeClass @JvmStatic
        fun createDatabase() {
            println("Looking for sqlite database at path: ${Config["db.template"]}")
            println("Working directory: ${System.getProperty("user.dir")}")

            val newDbFile = File(Config["db.location"])
            val source = File(Config["db.template"])

            source.copyTo(newDbFile, true)
        }

        @AfterClass @JvmStatic
        fun dropDatabase() {
            File(Config["db.location"]).delete()
        }

    }

    @Before
    fun clearDatabase() {
        JooqContext().use {
            it.dsl.deleteFrom(ORDERLY)
                    .execute()
        }
    }

}