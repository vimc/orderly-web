package org.vaccineimpact.orderlyweb.db

import org.vaccineimpact.orderlyweb.security.OnetimeTokenStore

object TokenStore
{
    val instance: OnetimeTokenStore = InMemoryTokenStore() //SQLiteTokenStore()
}
