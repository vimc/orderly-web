package org.vaccineimpact.orderlyweb.db

import org.jooq.Record
import org.vaccineimpact.orderlyweb.db.Tables.*
import org.vaccineimpact.orderlyweb.db.tables.records.OrderlywebUserRecord
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.User
import org.vaccineimpact.orderlyweb.models.UserSource
import java.time.Instant

interface UserRepository
{
    fun addUser(email: String, username: String, displayName: String, source: UserSource)
    fun getUser(email: String): User?
    fun getReportReaders(reportName: String): Map<User, Scope>
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
                        .set(Tables.ORDERLYWEB_USER.USER_SOURCE, source.toString())
                        .set(Tables.ORDERLYWEB_USER.LAST_LOGGED_IN, now)
                        .where(Tables.ORDERLYWEB_USER.EMAIL.eq(email))
                        .execute()
            }
        }
    }

    override fun getReportReaders(reportName: String): Map<User, Scope>
    {
        //Returns all users which can read the report, along with their report read scope (global or report-specific)
        JooqContext().use {
            val result = it.dsl.select(ORDERLYWEB_USER.USERNAME, ORDERLYWEB_USER.DISPLAY_NAME, ORDERLYWEB_USER.EMAIL,
                            ORDERLYWEB_USER.USER_SOURCE, ORDERLYWEB_USER.LAST_LOGGED_IN,
                            ORDERLYWEB_USER_GROUP_GLOBAL_PERMISSION.ID, ORDERLYWEB_USER_GROUP_REPORT_PERMISSION.ID)
                    .from(ORDERLYWEB_USER)
                    .join(ORDERLYWEB_USER_GROUP)
                    .on(ORDERLYWEB_USER.EMAIL.eq(ORDERLYWEB_USER_GROUP.ID))
                    .join(ORDERLYWEB_USER_GROUP_PERMISSION)
                    .on(ORDERLYWEB_USER_GROUP_PERMISSION.USER_GROUP.eq(ORDERLYWEB_USER_GROUP.ID))
                    .leftJoin(ORDERLYWEB_USER_GROUP_GLOBAL_PERMISSION)
                    .on(ORDERLYWEB_USER_GROUP_PERMISSION.ID.eq(ORDERLYWEB_USER_GROUP_GLOBAL_PERMISSION.ID))
                    .leftJoin(ORDERLYWEB_USER_GROUP_REPORT_PERMISSION)
                    .on(ORDERLYWEB_USER_GROUP_PERMISSION.ID.eq(ORDERLYWEB_USER_GROUP_REPORT_PERMISSION.ID))
                    .where(ORDERLYWEB_USER_GROUP_PERMISSION.PERMISSION.eq("reports.read"))
                    .and(ORDERLYWEB_USER_GROUP_GLOBAL_PERMISSION.ID.isNotNull.or(ORDERLYWEB_USER_GROUP_REPORT_PERMISSION.REPORT.eq(reportName)))
                    .fetch()

            return result.map{
                it.into(User::class.java) to
                        if (it[ORDERLYWEB_USER_GROUP_GLOBAL_PERMISSION.ID] != null)
                            Scope.Global()
                        else
                            Scope.Specific("report", reportName)
            }.toMap()

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