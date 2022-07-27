package org.vaccineimpact.orderlyweb.db

object TokenStore
{
    val instance: OnetimeTokenStore = InMemoryTokenStore() // SQLiteTokenStore()
}
