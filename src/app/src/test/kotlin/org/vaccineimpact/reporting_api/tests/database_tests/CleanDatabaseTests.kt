package org.vaccineimpact.reporting_api.tests.database_tests

import org.jooq.Table
import org.junit.Before
import org.vaccineimpact.reporting_api.db.JooqContext
import org.vaccineimpact.reporting_api.db.Tables

abstract class CleanDatabaseTests: DatabaseTests()
{

    @Before
    fun clearDatabase()
    {

        val tables = Tables::class.java;
        val fields = tables.declaredFields;


        JooqContext().use {

            for (field in fields){
                it.dsl.deleteFrom(field.get(null) as Table<*>)
                        .execute()
            }

        }
    }


}