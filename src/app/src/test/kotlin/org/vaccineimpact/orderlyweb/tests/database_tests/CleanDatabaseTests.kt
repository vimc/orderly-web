package org.vaccineimpact.orderlyweb.tests.database_tests

import org.jooq.Table
import org.junit.Before
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.Tables

abstract class CleanDatabaseTests : DatabaseTests()
{
    @Before
    fun clearDatabase()
    {
        val tables = Tables::class.java

        val enumTables = listOf("ARTEFACT_FORMAT", "FILE_PURPOSE", "ORDERLYWEB_PERMISSION", "CHANGELOG_LABEL")
        val fields = tables.declaredFields.filter {
            !enumTables.contains(it.name)
        }

        JooqContext(enableForeignKeyConstraints = false).use {
            for (field in fields)
            {
                it.dsl.deleteFrom(field.get(null) as Table<*>)
                        .execute()
            }

        }
    }
}