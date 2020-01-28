package org.vaccineimpact.orderlyweb.models

import org.vaccineimpact.orderlyweb.models.permissions.PermissionSet
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import java.beans.ConstructorProperties
import java.time.Instant

data class UserDetails
@ConstructorProperties("username", "displayName", "email", "userSource", "lastLoggedIn")
constructor(val username: String,
            val displayName: String,
            val email: String,
            val source: String,
            val lastLoggedIn: Instant
)

data class User
@ConstructorProperties("username", "displayName", "email")
constructor(val username: String,
            val displayName: String,
            val email: String
)

enum class UserSource
{
    Montagu,
    GitHub,
    CLI
}
