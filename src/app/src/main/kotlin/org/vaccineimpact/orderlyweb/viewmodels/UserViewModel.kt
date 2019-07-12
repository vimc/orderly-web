package org.vaccineimpact.orderlyweb.viewmodels

import org.vaccineimpact.orderlyweb.models.User

data class UserViewModel(val email: String, val username: String, val displayName: String)
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

            return UserViewModel(user.email, user.username, displayName)
        }

        private fun isNotEmptyOrUnknown(value: String): Boolean
        {
            return !(value.isEmpty() || value == "unknown")
        }

    }
}