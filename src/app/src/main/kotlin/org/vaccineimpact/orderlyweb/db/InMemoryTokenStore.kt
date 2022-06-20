package org.vaccineimpact.orderlyweb.db

class InMemoryTokenStore : OnetimeTokenStore
{
    private val tokens = mutableSetOf<String>()

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