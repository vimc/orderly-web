package org.vaccineimpact.orderlyweb.test_helpers

import org.jooq.Table
import org.junit.Before
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.Tables

abstract class CleanDatabaseTests : DatabaseTests()
{
    @Before
    fun createDatabase()
    {
        val tables = Tables::class.java

        val enumTables = listOf("ARTEFACT_FORMAT", "FILE_PURPOSE", "ORDERLYWEB_PERMISSION", "CHANGELOG_LABEL")
        val views = listOf("ORDERLYWEB_USER_GROUP_PERMISSION_ALL", "ORDERLYWEB_REPORT_VERSION_FULL")
        val fields = tables.declaredFields.filter {
            !enumTables.contains(it.name) && !views.contains(it.name)
        }

        JooqContext(enableForeignKeyConstraints = false).use {
            for (field in fields)
            {
                // jacoco code coverage plugin adds synthetic fields which we can't access here
                if (!field.isSynthetic)
                {
                    it.dsl.deleteFrom(field.get(null) as Table<*>)
                            .execute()
                }
            }

        }

        insertCustomFields()
    }

}