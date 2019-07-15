package org.vaccineimpact.orderlyweb.db

import org.jooq.Record
import org.vaccineimpact.orderlyweb.db.Tables.*
import org.vaccineimpact.orderlyweb.models.User
import org.vaccineimpact.orderlyweb.models.UserDetails
import org.vaccineimpact.orderlyweb.models.UserSource
import org.vaccineimpact.orderlyweb.models.permissions.Role
import java.time.Instant

interface RoleRepository
{
    fun getGlobalReportReaderRoles(): List<Role>
    fun getScopedReportReaderRoles(reportName: String): List<Role>
    fun getAllRoleNames(): List<String>
}

class OrderlyRoleRepository(private val userMapper: UserMapper = UserMapper()) : RoleRepository
{
    override fun getAllRoleNames(): List<String>
    {
        return JooqContext().use {
            it.dsl.select(ORDERLYWEB_USER_GROUP.ID)
                    .from(ORDERLYWEB_USER_GROUP)
                    .leftOuterJoin(ORDERLYWEB_USER)
                    .on(ORDERLYWEB_USER_GROUP.ID.eq(ORDERLYWEB_USER.EMAIL))
                    .where(ORDERLYWEB_USER.EMAIL.isNull)
                    .fetchInto(String::class.java)
        }
    }

    private fun reportReadingGroupsQuery(db: JooqContext) = db.dsl.select(ORDERLYWEB_USER_GROUP.ID,
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

    override fun getGlobalReportReaderRoles(): List<Role>
    {
        return JooqContext().use {

            reportReadingGroupsQuery(it)
                    .and(permissionIsGlobal())
                    .fetch()
                    .groupBy { r -> r[ORDERLYWEB_USER_GROUP.ID] }
                    .map(::mapUserGroup)
        }
    }

    override fun getScopedReportReaderRoles(reportName: String): List<Role>
    {
        return JooqContext().use {

            reportReadingGroupsQuery(it)
                    .and(permissionIsScopedToReport(reportName))
                    .fetch()
                    .groupBy { r -> r[ORDERLYWEB_USER_GROUP.ID] }
                    .map(::mapUserGroup)
        }
    }

    private fun mapUserGroup(group: Map.Entry<String, List<Record>>): Role
    {
        return Role(group.key, group.value.map(userMapper::mapUser))
    }

}