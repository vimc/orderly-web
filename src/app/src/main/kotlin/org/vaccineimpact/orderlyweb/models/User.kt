package org.vaccineimpact.orderlyweb.models

import java.beans.ConstructorProperties
import java.time.Instant

interface UserIdentity
{
    val username: String
    val displayName: String
    val email: String
}

data class UserDetails
@ConstructorProperties("username", "displayName", "email", "userSource", "lastLoggedIn")
constructor(override val username: String,
            override val displayName: String,
            override val email: String,
            val source: String,
            val lastLoggedIn: Instant
): UserIdentity

data class User
@ConstructorProperties("username", "displayName", "email")
constructor(override val username: String,
            override val displayName: String,
            override val email: String
): UserIdentity


enum class UserSource
{
    Montagu,
    GitHub,
    CLI
}
