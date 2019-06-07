package org.vaccineimpact.orderlyweb.viewmodels

import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.User

data class ReportReaderViewModel(val email: String, val username: String, val displayName: String, val canRemove: Boolean)
{
    companion object
    {
        fun build(user: User, scope: Scope): ReportReaderViewModel
        {
            val canRemove = scope is Scope.Specific
            val displayName = if
                (isNotEmptyOrUnknown(user.displayName))
                user.displayName
            else if (isNotEmptyOrUnknown(user.username))
                user.username

            else
                user.email

            return ReportReaderViewModel(user.email, user.username, displayName, canRemove)
        }

        private fun isNotEmptyOrUnknown(value: String): Boolean
        {
            return !(value.isEmpty() || value == "unknown")
        }

    }
}