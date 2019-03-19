package org.vaccineimpact.orderlyweb.models

import java.beans.ConstructorProperties
import java.time.Instant

data class User
@ConstructorProperties("username", "displayName", "email", "userSource", "lastLoggedIn")
constructor(val username: String,
            val displayName: String,
            val email: String,
            val source: String,
            val lastLoggedIn: Instant
)

enum class UserSource {
    Montagu,
    GitHub
}
