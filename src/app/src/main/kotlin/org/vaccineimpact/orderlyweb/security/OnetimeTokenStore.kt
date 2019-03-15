package org.vaccineimpact.orderlyweb.security

interface OnetimeTokenStore
{
    fun setup()
    fun storeToken(token: String)
    fun validateOneTimeToken(token: String): Boolean
}
