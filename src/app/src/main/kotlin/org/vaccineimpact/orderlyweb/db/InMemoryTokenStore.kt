package org.vaccineimpact.orderlyweb.db

import org.vaccineimpact.orderlyweb.security.OnetimeTokenStore

class InMemoryTokenStore : OnetimeTokenStore
{
    private var tokens = mutableSetOf<String>()

    override fun setup()
    {
    }

    @Synchronized
    override fun storeToken(uncompressedToken: String)
    {
        tokens.add(uncompressedToken)
    }

    @Synchronized
    override fun validateOneTimeToken(uncompressedToken: String): Boolean
    {
        return tokens.remove(uncompressedToken)
    }
}