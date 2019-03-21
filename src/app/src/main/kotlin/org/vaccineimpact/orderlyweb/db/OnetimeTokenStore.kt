package org.vaccineimpact.orderlyweb.db

interface OnetimeTokenStore
{
    fun setup()
    fun storeToken(token: String)
    fun validateOneTimeToken(token: String): Boolean
}
