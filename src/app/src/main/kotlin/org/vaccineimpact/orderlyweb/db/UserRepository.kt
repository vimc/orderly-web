package org.vaccineimpact.orderlyweb.db

import org.jooq.Record
import org.vaccineimpact.orderlyweb.db.Tables.*
import org.vaccineimpact.orderlyweb.models.User
import org.vaccineimpact.orderlyweb.models.UserDetails
import org.vaccineimpact.orderlyweb.models.UserSource
import org.vaccineimpact.orderlyweb.models.permissions.UserGroup
import org.vaccineimpact.orderlyweb.models.permissions.UserGroupPermission
import java.time.Instant

interface UserRepository
{
    fun addUser(email: String, username: String, displayName: String, source: UserSource)
    fun getUser(email: String): UserDetails?
    fun getReportReaders(reportName: String): Map<User, List<UserGroupPermission>>
    fun getGlobalReportReaderGroups(): List<UserGroup>
}

class OrderlyUserRepository(private val permissionMapper: PermissionMapper = PermissionMapper()) : UserRepository
{
    override fun getGlobalReportReaderGroups(): List<UserGroup>
    {
        return JooqContext().use {

            it.dsl.select(ORDERLYWEB_USER_GROUP.ID,
                    ORDERLYWEB_USER.USERNAME,
                    ORDERLYWEB_USER.DISPLAY_NAME,
                    ORDERLYWEB_USER.EMAIL)
                    .fromJoinPath(ORDERLYWEB_USER_GROUP,
                            ORDERLYWEB_USER_GROUP_USER,
                            ORDERLYWEB_USER)
                    .join(ORDERLYWEB_USER_GROUP_PERMISSION_ALL)
                    .on(ORDERLYWEB_USER_GROUP_PERMISSION_ALL.USER_GROUP.eq(ORDERLYWEB_USER_GROUP.ID))
                    .where(ORDERLYWEB_USER_GROUP_PERMISSION_ALL.PERMISSION.eq("reports.read"))
                    .and(ORDERLYWEB_USER_GROUP.ID.ne(ORDERLYWEB_USER.EMAIL))
                    .and(permissionIsGlobal())
                    .fetch()
                    .groupBy { r -> r[ORDERLYWEB_USER_GROUP.ID] }
                    .map(::mapUserGroup)
        }
    }

    private fun mapUserGroup(group: Map.Entry<String, List<Record>>): UserGroup
    {
        return UserGroup(group.key, group.value.map(::mapUser))
    }

    private fun mapUser(record: Record): User
    {
        return User(record[ORDERLYWEB_USER.USERNAME],
                record[ORDERLYWEB_USER.DISPLAY_NAME],
                record[ORDERLYWEB_USER.EMAIL])
    }

    override fun getReportReaders(reportName: String): Map<User, List<UserGroupPermission>>
    {
        //Returns all users which can read the report, along with the set of all relevant permissions
        // (global or report-specific, and the user groups from which they are derived)
        JooqContext().use {
            val result = it.dsl.select(ORDERLYWEB_USER.USERNAME,
                    ORDERLYWEB_USER.DISPLAY_NAME,
                    ORDERLYWEB_USER.EMAIL,
                    ORDERLYWEB_USER_GROUP.ID,
                    ORDERLYWEB_USER_GROUP_PERMISSION_ALL.PERMISSION,
                    ORDERLYWEB_USER_GROUP_PERMISSION_ALL.SCOPE_PREFIX,
                    ORDERLYWEB_USER_GROUP_PERMISSION_ALL.SCOPE_ID)
                    .fromJoinPath(ORDERLYWEB_USER_GROUP,
                            ORDERLYWEB_USER_GROUP_USER,
                            ORDERLYWEB_USER)

                    .join(ORDERLYWEB_USER_GROUP_PERMISSION_ALL)
                    .on(ORDERLYWEB_USER_GROUP_PERMISSION_ALL.USER_GROUP.eq(ORDERLYWEB_USER_GROUP.ID))
                    .where(ORDERLYWEB_USER_GROUP_PERMISSION_ALL.PERMISSION.eq("reports.read"))
                    .and(permissionIsGlobal().or(permissionIsScopedToReport(reportName)))
                    .fetch()

            return result.map(permissionMapper::mapUserGroupPermission)
                    .groupBy({ pair -> pair.first }, { pair -> pair.second })
        }
    }

    private fun permissionIsGlobal() = ORDERLYWEB_USER_GROUP_PERMISSION_ALL.SCOPE_PREFIX.eq("*")
    private fun permissionIsScopedToReport(report: String) =
            ORDERLYWEB_USER_GROUP_PERMISSION_ALL.SCOPE_PREFIX.eq("report").and(ORDERLYWEB_USER_GROUP_PERMISSION_ALL.SCOPE_ID.eq(report))


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

    private fun getUserRecord(email: String, db: JooqContext): Record?
    {
        return db.dsl.select(ORDERLYWEB_USER.USERNAME, ORDERLYWEB_USER.DISPLAY_NAME, ORDERLYWEB_USER.EMAIL,
                ORDERLYWEB_USER.USER_SOURCE, ORDERLYWEB_USER.LAST_LOGGED_IN)
                .from(ORDERLYWEB_USER)
                .where(ORDERLYWEB_USER.EMAIL.eq(email))
                .singleOrNull()
    }

}