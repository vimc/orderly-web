package org.vaccineimpact.orderlyweb.db

import org.jooq.DSLContext
import org.vaccineimpact.orderlyweb.db.Tables.ORDERLYWEB_PERMISSION
import org.vaccineimpact.orderlyweb.db.Tables.ORDERLYWEB_USER_GROUP_PERMISSION
import org.vaccineimpact.orderlyweb.errors.UnknownObjectError
import org.vaccineimpact.orderlyweb.models.UserSource
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import java.time.Instant

interface UserRepository
{
    fun addUser(email: String, username: String, displayName: String, source: UserSource)
}

class OrderlyUserRepository : UserRepository
{
    override fun addUser(email: String, username: String, displayName: String, source: UserSource)
    {
        val now = Instant.now().toString()
        JooqContext().use {

            val user = it.dsl.selectFrom(Tables.ORDERLYWEB_USER)
                    .where(Tables.ORDERLYWEB_USER.EMAIL.eq(email))
                    .singleOrNull()

            if (user == null)
            {
                it.dsl.newRecord(Tables.ORDERLYWEB_USER)
                        .apply {
                            this.username = username
                            this.displayName = displayName
                            this.email = email
                            this.userSource = source.toString()
                            this.lastLoggedIn = now
                        }.store()

                it.dsl.newRecord(Tables.ORDERLYWEB_USER_GROUP)
                        .apply {
                            this.id = email
                        }.store()
            }
            else
            {
                it.dsl.update(Tables.ORDERLYWEB_USER)
                        .set(Tables.ORDERLYWEB_USER.USERNAME, username)
                        .set(Tables.ORDERLYWEB_USER.USER_SOURCE, source.toString())
                        .set(Tables.ORDERLYWEB_USER.LAST_LOGGED_IN, now)
                        .where(Tables.ORDERLYWEB_USER.EMAIL.eq(email))
                        .execute()
            }
        }
    }

}