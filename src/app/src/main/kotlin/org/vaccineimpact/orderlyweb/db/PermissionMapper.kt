package org.vaccineimpact.orderlyweb.db

import org.jooq.Record
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission

class PermissionMapper
{
    fun mapPermission(dbPermission: Record): ReifiedPermission
    {
        return ReifiedPermission(dbPermission[Tables.ORDERLYWEB_USER_GROUP_PERMISSION_ALL.PERMISSION], mapScope(dbPermission))
    }

    private fun mapScope(dbScope: Record): Scope
    {
        return if (dbScope[Tables.ORDERLYWEB_USER_GROUP_PERMISSION_ALL.SCOPE_PREFIX] == "*")
            Scope.Global()
        else
            Scope.Specific(dbScope[Tables.ORDERLYWEB_USER_GROUP_PERMISSION_ALL.SCOPE_PREFIX] as String,
                    dbScope[Tables.ORDERLYWEB_USER_GROUP_PERMISSION_ALL.SCOPE_ID])
    }
}