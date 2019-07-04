package org.vaccineimpact.orderlyweb.viewmodels

import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.User
import org.vaccineimpact.orderlyweb.models.UserDetails
import org.vaccineimpact.orderlyweb.models.UserIdentity
import org.vaccineimpact.orderlyweb.models.permissions.UserGroupPermission

data class ReportReaderViewModel(val email: String, val username: String, val displayName: String, val canRemove: Boolean)
{
    companion object
    {
        fun build(userDetails: UserIdentity, permissions: List<UserGroupPermission>): ReportReaderViewModel
        {
            //the report reader can only be removed if they only have read permission for this report which is scoped to
            //the report (not global) and do not have permission through belonging to a user group other than their
            //identity group
            val canRemove = permissions.all{ it.userGroup == userDetails.email && it.permission.scope is Scope.Specific}
            val displayName = when
            {
                isNotEmptyOrUnknown(userDetails.displayName) -> userDetails.displayName
                isNotEmptyOrUnknown(userDetails.username) -> userDetails.username
                else -> userDetails.email
            }

            return ReportReaderViewModel(userDetails.email, userDetails.username, displayName, canRemove)
        }

        fun build(userDetails: UserIdentity, canRemove: Boolean): ReportReaderViewModel
        {
           val displayName = when
            {
                isNotEmptyOrUnknown(userDetails.displayName) -> userDetails.displayName
                isNotEmptyOrUnknown(userDetails.username) -> userDetails.username
                else -> userDetails.email
            }

            return ReportReaderViewModel(userDetails.email, userDetails.username, displayName, canRemove)
        }

        private fun isNotEmptyOrUnknown(value: String): Boolean
        {
            return !(value.isEmpty() || value == "unknown")
        }

    }
}