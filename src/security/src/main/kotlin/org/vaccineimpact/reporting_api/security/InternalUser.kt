package org.vaccineimpact.reporting_api.security

data class InternalUser(
        val username: String,
        val roles: String,
        val permissions: String
)