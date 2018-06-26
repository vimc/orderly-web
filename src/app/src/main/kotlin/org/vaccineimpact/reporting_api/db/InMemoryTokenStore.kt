package org.vaccineimpact.reporting_api.db

import org.vaccineimpact.reporting_api.security.OnetimeTokenStore

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