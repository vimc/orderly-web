package org.vaccineimpact.orderlyweb.viewmodels

import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission

data class PermissionViewModel(val name: String, val scope: String)
{
    companion object
    {
        fun build(permission: ReifiedPermission): PermissionViewModel
        {
            return PermissionViewModel(permission.name, permission.scope.value);
        }
    }
}