package org.vaccineimpact.reporting_api.security

interface OnetimeTokenStore
{
    fun setup()
    fun storeToken(uncompressedToken: String)
    fun validateOneTimeToken(uncompressedToken: String): Boolean
}
