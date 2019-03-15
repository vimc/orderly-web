package org.vaccineimpact.orderlyweb.models

import java.beans.ConstructorProperties

data class User
@ConstructorProperties("username", "email", "userSource")
constructor(val username: String,
            val email: String,
            val source: String
)
