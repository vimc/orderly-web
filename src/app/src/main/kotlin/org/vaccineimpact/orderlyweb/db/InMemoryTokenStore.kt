package org.vaccineimpact.orderlyweb.db

import org.vaccineimpact.orderlyweb.security.OnetimeTokenStore

class InMemoryTokenStore : OnetimeTokenStore
{
    private var tokens = mutableSetOf<String>()

    override fun setup()
    {
    }

    @Synchronized
    override fun storeToken(token: String)
    {
        tokens.add(token)
    }

    @Synchronized
    override fun validateOneTimeToken(token: String): Boolean
    {
        return tokens.remove(token)
    }
}