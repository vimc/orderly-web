package org.vaccineimpact.orderlyweb.security

data class InternalUser(
        val username: String,
        val roles: String,
        val permissions: String
)