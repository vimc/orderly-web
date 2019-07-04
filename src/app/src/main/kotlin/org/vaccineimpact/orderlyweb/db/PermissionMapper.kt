package org.vaccineimpact.orderlyweb.db

import org.jooq.Record
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.UserDetails
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.models.permissions.UserGroupPermission

class PermissionMapper
{
    fun mapPermission(dbPermission: Record): ReifiedPermission
    {
        return ReifiedPermission(dbPermission[Tables.ORDERLYWEB_USER_GROUP_PERMISSION_ALL.PERMISSION], mapScope(dbPermission))
    }

    fun mapScope(dbScope: Record): Scope
    {
        return if (dbScope[Tables.ORDERLYWEB_USER_GROUP_PERMISSION_ALL.SCOPE_PREFIX] == "*")
            Scope.Global()
        else
            Scope.Specific(dbScope[Tables.ORDERLYWEB_USER_GROUP_PERMISSION_ALL.SCOPE_PREFIX] as String,
                    dbScope[Tables.ORDERLYWEB_USER_GROUP_PERMISSION_ALL.SCOPE_ID])
    }

    fun mapUserGroupPermission(record: Record): Pair<UserDetails, UserGroupPermission>
    {
        return record.into(UserDetails::class.java) to
                UserGroupPermission(record[Tables.ORDERLYWEB_USER_GROUP.ID], mapPermission(record))
    }
}