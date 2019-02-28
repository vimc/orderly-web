package org.vaccineimpact.orderlyweb.security

interface OnetimeTokenStore
{
    fun setup()
    fun storeToken(uncompressedToken: String)
    fun validateOneTimeToken(uncompressedToken: String): Boolean
}
