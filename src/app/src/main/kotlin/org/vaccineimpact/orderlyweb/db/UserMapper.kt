package org.vaccineimpact.orderlyweb.db

import org.jooq.Record
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.User
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission

class UserMapper
{
    fun mapUser(record: Record): User
    {
        return User(record[Tables.ORDERLYWEB_USER.USERNAME],
                record[Tables.ORDERLYWEB_USER.DISPLAY_NAME],
                record[Tables.ORDERLYWEB_USER.EMAIL])
    }
}