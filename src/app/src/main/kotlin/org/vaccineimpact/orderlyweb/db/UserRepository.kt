package org.vaccineimpact.orderlyweb.db

import org.jooq.Record
import org.vaccineimpact.orderlyweb.db.Tables.*
import org.vaccineimpact.orderlyweb.models.User
import org.vaccineimpact.orderlyweb.models.UserSource
import java.time.Instant

interface UserRepository
{
    fun addUser(email: String, username: String, displayName: String, source: UserSource)
    fun getUser(email: String): User?
}

class OrderlyUserRepository : UserRepository
{
    override fun getUser(email: String): User?
    {
        return JooqContext().use {
            getUserRecord(email, it)?.into(User::class.java)
        }
    }

    override fun addUser(email: String, username: String, displayName: String, source: UserSource)
    {
        val now = Instant.now().toString()
        JooqContext().use {

            val user = getUserRecord(email, it)

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

                it.dsl.newRecord(Tables.ORDERLYWEB_USER_GROUP_USER)
                        .apply {
                            this.userGroup = email
                            this.email = email
                        }.insert()
            }
            else
            {
                it.dsl.update(Tables.ORDERLYWEB_USER)
                        .set(Tables.ORDERLYWEB_USER.USERNAME, username)
                        .set(Tables.ORDERLYWEB_USER.DISPLAY_NAME, displayName)
                        .set(Tables.ORDERLYWEB_USER.USER_SOURCE, source.toString())
                        .set(Tables.ORDERLYWEB_USER.LAST_LOGGED_IN, now)
                        .where(Tables.ORDERLYWEB_USER.EMAIL.eq(email))
                        .execute()
            }
        }
    }

    private fun getUserRecord(email: String, db: JooqContext): Record?
    {
        return db.dsl.select(ORDERLYWEB_USER.USERNAME, ORDERLYWEB_USER.DISPLAY_NAME, ORDERLYWEB_USER.EMAIL,
                ORDERLYWEB_USER.USER_SOURCE, ORDERLYWEB_USER.LAST_LOGGED_IN)
                .from(ORDERLYWEB_USER)
                .where(ORDERLYWEB_USER.EMAIL.eq(email))
                .singleOrNull()
    }

}