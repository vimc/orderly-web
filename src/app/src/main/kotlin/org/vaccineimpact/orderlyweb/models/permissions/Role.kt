package org.vaccineimpact.orderlyweb.models.permissions

import org.vaccineimpact.orderlyweb.models.Scope
import java.beans.ConstructorProperties

data class ReifiedRole(
        val name: String,
        val scope: Scope
)
{
    override fun toString() = "$scope/$name"
}


data class RoleAssignment @ConstructorProperties("name", "scopePrefix", "scopeId")
constructor(val name: String, var scopePrefix: String?, var scopeId: String?)
{
    constructor(role: ReifiedRole) : this(role.name, role.scope.databaseScopePrefix, role.scope.databaseScopeId)
}

data class AssociateRole(val action: String,
                         val name: String,
                         val scopePrefix: String?,
                         val scopeId: String?)