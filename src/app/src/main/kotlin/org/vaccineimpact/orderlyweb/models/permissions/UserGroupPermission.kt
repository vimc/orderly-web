package org.vaccineimpact.orderlyweb.models.permissions

data class UserGroupPermission(val userGroup: String, val permission: ReifiedPermission)
{
    override fun equals(other: Any?): Boolean = when (other)
    {
        is UserGroupPermission -> other.userGroup == userGroup && other.permission.toString() == permission.toString()
        else -> false
    }

    override fun hashCode(): Int
    {
        var result = userGroup.hashCode()
        result = 31 * result + permission.hashCode()
        return result
    }

}