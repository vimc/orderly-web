package org.vaccineimpact.orderlyweb.models

import java.beans.ConstructorProperties

data class User
@ConstructorProperties("username", "email")
constructor(val username: String,
            val email: String
)
