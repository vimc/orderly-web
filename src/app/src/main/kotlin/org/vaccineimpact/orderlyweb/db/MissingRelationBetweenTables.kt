package org.vaccineimpact.orderlyweb.db

class MissingRelationBetweenTables(from: org.jooq.Table<*>, to: org.jooq.Table<*>) : Exception(
        "Attempted to construct join from ${from.name} to ${to.name}, but there are no keys relating those tables."
)