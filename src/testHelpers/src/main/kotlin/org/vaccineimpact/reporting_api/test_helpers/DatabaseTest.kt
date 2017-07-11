package org.vaccineimpact.reporting_api.test_helpers

import org.junit.Before
import org.vaccineimpact.reporting_api.db.Config
import org.vaccineimpact.reporting_api.db.JooqContext
import org.vaccineimpact.reporting_api.db.Tables.*
import java.io.File

abstract class DatabaseTests: MontaguTests()
{
    @Before
    fun clearDatabase()
    {
        JooqContext(File(Config["dbTest.location"]).absolutePath).use{
            it.dsl.deleteFrom(ORDERLY)
                    .execute()
        }
    }
}