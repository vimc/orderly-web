package org.vaccineimpact.orderlyweb.viewmodels

import org.vaccineimpact.orderlyweb.models.permissions.Role

data class RoleViewModel(val name: String, val members: List<UserViewModel>, val permissions: List<PermissionViewModel>)
{
    companion object
    {
        fun build(role: Role): RoleViewModel
        {
            return RoleViewModel(role.name,
                    role.members.map {
                        UserViewModel.build(it)
                    }.sortedBy { it.displayName },
                    role.permissions.map {
                        PermissionViewModel.build(it, role.name)
                    }.sorted())
        }
    }
}
