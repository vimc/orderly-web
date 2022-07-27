package org.vaccineimpact.orderlyweb.viewmodels

import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission

data class PermissionViewModel(
        val name: String,
        val scopePrefix: String?,
        val scopeId: String?,
        val source: String
)
{
    companion object
    {
        fun build(permission: ReifiedPermission, source: String): PermissionViewModel
        {
            return PermissionViewModel(
                    permission.name,
                    permission.scope.databaseScopePrefix,
                    permission.scope.databaseScopeId,
                    source
            )
        }
    }
}

fun Iterable<PermissionViewModel>.sorted(): List<PermissionViewModel>
{
    return this.sortedWith(compareBy(PermissionViewModel::name, PermissionViewModel::scopeId))
}
