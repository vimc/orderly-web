package org.vaccineimpact.reporting_api.security

interface OnetimeTokenStore
{
    fun validateOneTimeToken(token: String): Boolean
}
