package org.vaccineimpact.reporting_api.security

interface OnetimeTokenStore
{
    fun setup()
    fun storeToken(token: String)
    fun validateOneTimeToken(token: String): Boolean
}
