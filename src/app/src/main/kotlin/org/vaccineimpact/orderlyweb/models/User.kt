package org.vaccineimpact.orderlyweb.models

import java.beans.ConstructorProperties
import java.time.Instant

data class User
@ConstructorProperties("username", "name", "email", "lastLoggedIn")
constructor(val username: String,
            val name: String,
            val email: String,
            val lastLoggedIn: Instant?
)
