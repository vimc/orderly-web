package org.vaccineimpact.orderlyweb.models

data class AuthenticationResponse(
        val tokenType: String = "bearer",
        val accessToken: String,
        val expiresIn: Long
)