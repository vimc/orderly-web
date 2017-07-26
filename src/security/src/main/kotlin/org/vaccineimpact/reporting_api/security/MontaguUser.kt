package org.vaccineimpact.reporting_api.security

import org.vaccineimpact.api.models.permissions.ReifiedPermission
import org.vaccineimpact.api.models.permissions.ReifiedRole
import java.beans.ConstructorProperties
import java.sql.Timestamp

data class MontaguUser(
        val properties: UserProperties,
        val roles: List<ReifiedRole>,
        val permissions: List<ReifiedPermission>
) : UserPropertiesInterface by properties

interface UserPropertiesInterface {
    val username: String
    val name: String
    val email: String
    val passwordHash: String
    val lastLoggedIn: Timestamp?
}

data class UserProperties
@ConstructorProperties("username", "name", "email", "passwordHash", "lastLoggedIn")
constructor(
        override val username: String,
        override val name: String,
        override val email: String,
        override val passwordHash: String,
        override val lastLoggedIn: Timestamp?
) : UserPropertiesInterface