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
            val displayName = if (user.displayName == "unknown")
                user.username
            else
                user.displayName

            return ReportReaderViewModel(user.email, user.username, displayName, canRemove)
        }
    }
}