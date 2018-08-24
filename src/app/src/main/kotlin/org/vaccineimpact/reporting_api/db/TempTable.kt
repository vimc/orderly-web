package org.vaccineimpact.reporting_api.db

import org.jooq.DSLContext
import org.jooq.Field
import org.jooq.Select
import org.jooq.impl.DSL
import org.jooq.impl.DSL.name

open class TempTable(val tableName: String, val query: Select<*>)
{
    inline fun <reified T> field(fieldName: String): Field<T> = DSL.field(name(tableName, fieldName), T::class.java)
}

fun DSLContext.withTemporaryTable(table: TempTable) = this.with(table.tableName).`as`(table.query)

fun Select<*>.asTemporaryTable(name: String) = TempTable(name, this)
