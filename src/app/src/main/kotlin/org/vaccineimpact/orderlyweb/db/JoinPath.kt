package org.vaccineimpact.orderlyweb.db

import org.jooq.*
import org.jooq.impl.TableImpl

/**
 * A JoinPath allows us to automatically construct the series of join operations needed
 * to get from type A -> B -> ... --> Z, assuming that there is a single foreign key between
 * each pair of tables, either from A->B or B->A.
 *
 * This is the same thing as jOOQ's join(...).onKey(), but because we have an ordering on the
 * tables we can avoid ambiguity which sometimes causes it to fail.
 *
 * It is intended that you would use this using the two extension methods below, `fromJoinPath`
 * at the beginning of a query, and `joinPath` to add more joins on to an existing query.
 */
class JoinPath(tables: Iterable<TableImpl<*>>, val joinType: JoinType)
{
    val steps = buildSteps(tables.toList()).toList()

    private fun buildSteps(tables: List<TableImpl<*>>): Iterable<JoinPathStep>
    {
        return tables.indices.drop(1).map { JoinPathStep(tables[it - 1], tables[it], joinType) }
    }

    fun <T : Record> doJoin(initialQuery: SelectJoinStep<T>): SelectJoinStep<T>
    {
        return steps.fold(initialQuery, { query, step -> step.doJoin(query) })
    }
}

@Suppress("UNCHECKED_CAST")
class JoinPathStep(
        private val from: TableImpl<*>,
        private val to: TableImpl<*>,
        private val joinType: JoinType = JoinType.JOIN)
{
    private val foreignKeyField: TableField<*, Any>
    private val primaryKeyField: Field<Any>

    init
    {
        val references = from.keys.flatMap { it.references }.filter { it.table == to } +
                to.keys.flatMap { it.references }.filter { it.table == from }
        val reference = references.singleOrNull()
                ?: throwKeyProblem(references)

        foreignKeyField = reference.fields.single() as TableField<*, Any>
        val targetTable = foreignKeyField.table.getOther(from, to)
        primaryKeyField = targetTable.primaryKey.fields.single() as Field<Any>
    }

    private fun throwKeyProblem(keys: Iterable<ForeignKey<*, *>>): ForeignKey<*, *>
    {
        throw when (keys.count())
        {
            0 -> MissingRelationBetweenTables(from, to)
            else -> AmbiguousRelationBetweenTables(from, to)
        }
    }

    fun <T : Record> doJoin(query: SelectJoinStep<T>): SelectJoinStep<T>
    {
        return query.join(to, joinType).on(foreignKeyField.eqField(primaryKeyField))
    }
}

fun <T : Record> SelectJoinStep<T>.joinPath(
        vararg tables: TableImpl<*>,
        joinType: JoinType = JoinType.JOIN
)
        : SelectJoinStep<T>
{
    return JoinPath(tables.toList(), joinType).doJoin(this)
}

fun <T : Record> SelectFromStep<T>.fromJoinPath(
        vararg tables: TableImpl<*>,
        joinType: JoinType = JoinType.JOIN
)
        : SelectJoinStep<T>
{
    val query = this.from(tables.first())
    return JoinPath(tables.toList(), joinType).doJoin(query)
}

fun <T> T.getOther(a: T, b: T) = when (this)
{
    a -> b
    b -> a
    else -> throw IllegalArgumentException("The given object '$this' was neither '$a' not '$b'")
}

// This helper avoids overloading ambiguity when the field type is "Any"
fun <T> TableField<*, T>.eqField(otherField: Field<T>): Condition = this.eq(otherField)