package org.vaccineimpact.orderlyweb.tests.database_tests

import org.jooq.Table
import org.junit.Before
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.Tables

abstract class CleanDatabaseTests: DatabaseTests()
{
    @Before
    override fun setup()
    {
        super.setup()

        val tables = Tables::class.java
        val fields = tables.declaredFields

        JooqContext(enableForeignKeyConstraints = false).use {
            for (field in fields){
                it.dsl.deleteFrom(field.get(null) as Table<*>)
                        .execute()
            }
        }
    }
}