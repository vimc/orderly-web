package org.vaccineimpact.orderlyweb.db.repositories

import org.jooq.Record
import org.jooq.Record3
import org.jooq.SelectConditionStep
import org.vaccineimpact.orderlyweb.db.*
import org.vaccineimpact.orderlyweb.db.Tables.*
import org.vaccineimpact.orderlyweb.models.User
import org.vaccineimpact.orderlyweb.models.UserDetails
import org.vaccineimpact.orderlyweb.models.UserSource
import java.time.Instant

interface UserRepository
{
    fun addUser(email: String, username: String, displayName: String, source: UserSource)
    fun getUser(email: String): UserDetails?
    fun getScopedReportReaderUsers(reportName: String): List<User>
    fun getGlobalReportReaderUsers(): List<User>
    fun getAllUsers(): List<User>
    fun getUserEmails(): List<String>
}

class OrderlyUserRepository(private val userMapper: UserMapper = UserMapper()) : UserRepository
{
    override fun getUserEmails(): List<String>
    {
        return JooqContext().use {
            it.dsl.select(ORDERLYWEB_USER.EMAIL)
                    .from(ORDERLYWEB_USER)
                    .fetchInto(String::class.java)
        }
    }

    override fun getScopedReportReaderUsers(reportName: String): List<User>
    {
        JooqContext().use {
            val result = it.reportReadersQuery()
                    .and(permissionIsScopedToReport(reportName))
                    .fetch()

            return result.map(userMapper::mapUser)
        }
    }

    override fun getGlobalReportReaderUsers(): List<User>
    {
        JooqContext().use {
            val result = it.reportReadersQuery()
                    .and(permissionIsGlobal())
                    .fetch()

            return result.map(userMapper::mapUser)
        }
    }

    override fun getAllUsers(): List<User>
    {
        return JooqContext().use {
            it.dsl.select(ORDERLYWEB_USER.USERNAME,
                    ORDERLYWEB_USER.DISPLAY_NAME,
                    ORDERLYWEB_USER.EMAIL)
                    .from(ORDERLYWEB_USER)
                    .fetch()
                    .map(userMapper::mapUser)
        }
    }

    override fun getUser(email: String): UserDetails?
    {
        return JooqContext().use {
            getUserRecord(email, it)?.into(UserDetails::class.java)
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

    private fun JooqContext.reportReadersQuery(): SelectConditionStep<Record3<String, String, String>>
    {
        return this.dsl.select(ORDERLYWEB_USER.USERNAME,
                ORDERLYWEB_USER.DISPLAY_NAME,
                ORDERLYWEB_USER.EMAIL)
                .fromJoinPath(ORDERLYWEB_USER_GROUP,
                        ORDERLYWEB_USER_GROUP_USER,
                        ORDERLYWEB_USER)

                .join(ORDERLYWEB_USER_GROUP_PERMISSION_ALL)
                .on(ORDERLYWEB_USER_GROUP_PERMISSION_ALL.USER_GROUP.eq(ORDERLYWEB_USER_GROUP.ID))
                .where(ORDERLYWEB_USER_GROUP_PERMISSION_ALL.PERMISSION.eq("reports.read"))
                .and(ORDERLYWEB_USER_GROUP.ID.eq(ORDERLYWEB_USER.EMAIL))
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
