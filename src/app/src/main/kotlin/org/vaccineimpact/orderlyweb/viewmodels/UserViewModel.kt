package org.vaccineimpact.orderlyweb.viewmodels

import org.vaccineimpact.orderlyweb.models.User
import org.vaccineimpact.orderlyweb.models.permissions.PermissionSet
import org.vaccineimpact.orderlyweb.models.permissions.Role

data class UserViewModel(val email: String, val username: String, val displayName: String,
                         val directPermissions: List<PermissionViewModel>,
                         val rolePermissions: List<PermissionViewModel>)
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

            return UserViewModel(user.email, user.username, displayName, listOf(), listOf())
        }

        fun build(user: User, directPermissions: PermissionSet, roles: List<Role>): UserViewModel
        {
            val directPermissionVms = directPermissions.map { PermissionViewModel.build(it, user.email) }
                    .sorted()

            val flatListOfRolePermissions = roles.flatMap { r ->
                r.permissions.map { p ->
                    PermissionViewModel.build(p, r.name)
                }
            }

            val rolePermissionVms = flatListOfRolePermissions
                    .groupBy { hashSetOf(it.name, it.scopeId, it.scopePrefix) }
                    .map { g ->
                        val permission = g.value.first()
                        val commaSeparatedSources = g.value
                                .sortedBy { it.source }
                                .joinToString { it.source }

                        permission.copy(source = commaSeparatedSources)
                    }.sorted()

            return build(user).copy(directPermissions = directPermissionVms,
                    rolePermissions = rolePermissionVms)
        }

        private fun isNotEmptyOrUnknown(value: String): Boolean
        {
            return !(value.isEmpty() || value == "unknown")
        }

    }
}
