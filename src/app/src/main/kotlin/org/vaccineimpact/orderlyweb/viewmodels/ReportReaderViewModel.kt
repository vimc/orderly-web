package org.vaccineimpact.orderlyweb.viewmodels

import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.User
import org.vaccineimpact.orderlyweb.models.permissions.UserGroupPermission

data class ReportReaderViewModel(val email: String, val username: String, val displayName: String, val canRemove: Boolean)
{
    companion object
    {
        fun build(user: User, permissions: List<UserGroupPermission>): ReportReaderViewModel
        {
            //the report reader can only be removed if they only have read permission for this report which is scoped to
            //the report (not global) and do not have permission through belonging to a user group other than their
            //identity group
            val canRemove = permissions.all{ it.userGroup == user.email && it.permission.scope is Scope.Specific}
            val displayName = when
            {
                isNotEmptyOrUnknown(user.displayName) -> user.displayName
                isNotEmptyOrUnknown(user.username) -> user.username
                else -> user.email
            }

            return ReportReaderViewModel(user.email, user.username, displayName, canRemove)
        }

        private fun isNotEmptyOrUnknown(value: String): Boolean
        {
            return !(value.isEmpty() || value == "unknown")
        }

    }
}