package org.vaccineimpact.orderlyweb.db

import org.jooq.Record
import org.vaccineimpact.orderlyweb.models.User

class UserMapper
{
    fun mapUser(record: Record): User
    {
        return User(record[Tables.ORDERLYWEB_USER.USERNAME],
                record[Tables.ORDERLYWEB_USER.DISPLAY_NAME],
                record[Tables.ORDERLYWEB_USER.EMAIL])
    }
}