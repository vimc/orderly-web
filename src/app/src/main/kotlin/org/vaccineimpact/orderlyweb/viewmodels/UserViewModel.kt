package org.vaccineimpact.orderlyweb.viewmodels

import org.vaccineimpact.orderlyweb.models.User
import org.vaccineimpact.orderlyweb.models.permissions.PermissionSet

data class UserViewModel(val email: String, val username: String, val displayName: String,
                         val permissions: List<PermissionViewModel>)
{
    companion object
    {
        fun build(user: User): UserViewModel
        {
            val displayName = when
            {
                isNotEmptyOrUnknown(user.displayName) -> user.displayName
                isNotEmptyOrUnknown(user.username) -> user.username
                else -> user.email
            }

            return UserViewModel(user.email, user.username, displayName, listOf())
        }

        fun build(user: User, permissions: PermissionSet): UserViewModel
        {
            return build(user).copy(permissions = permissions.map { PermissionViewModel.build(it) }
                    .sortedWith(compareBy(PermissionViewModel::name, PermissionViewModel::scopeId)))
        }

        private fun isNotEmptyOrUnknown(value: String): Boolean
        {
            return !(value.isEmpty() || value == "unknown")
        }

    }
}