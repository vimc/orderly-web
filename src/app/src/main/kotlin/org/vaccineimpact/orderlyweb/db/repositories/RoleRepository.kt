package org.vaccineimpact.orderlyweb.db.repositories

import org.jooq.JoinType
import org.jooq.Record
import org.vaccineimpact.orderlyweb.db.*
import org.vaccineimpact.orderlyweb.db.Tables.*
import org.vaccineimpact.orderlyweb.models.permissions.Role

interface RoleRepository
{
    fun getGlobalReportReaderRoles(): List<Role>
    fun getScopedReportReaderRoles(reportName: String): List<Role>
    fun getAllRoleNames(): List<String>
    fun getAllRoles(): List<Role>

    companion object {
        const val ADMIN_ROLE = "Admin"
    }
}

class OrderlyRoleRepository(private val userMapper: UserMapper = UserMapper(),
                            private val authRepo: AuthorizationRepository = OrderlyAuthorizationRepository()) :
        RoleRepository
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

    override fun getAllRoles(): List<Role>
    {
        val roleNames = getAllRoleNames()
        return JooqContext().use {
            it.dsl.select(ORDERLYWEB_USER_GROUP.ID,
                    ORDERLYWEB_USER.USERNAME,
                    ORDERLYWEB_USER.DISPLAY_NAME,
                    ORDERLYWEB_USER.EMAIL)
                    .fromJoinPath(ORDERLYWEB_USER_GROUP,
                            ORDERLYWEB_USER_GROUP_USER,
                            ORDERLYWEB_USER, joinType = JoinType.LEFT_OUTER_JOIN)
                    .where(ORDERLYWEB_USER_GROUP.ID.`in`(roleNames))
                    .fetch()
                    .groupBy { r -> r[ORDERLYWEB_USER_GROUP.ID]}
                    .map(::mapUserGroup)
        }
    }

    private fun reportReadingGroupsQuery(db: JooqContext) = db.dsl.select(ORDERLYWEB_USER_GROUP.ID,
            ORDERLYWEB_USER.USERNAME,
            ORDERLYWEB_USER.DISPLAY_NAME,
            ORDERLYWEB_USER.EMAIL)
            .fromJoinPath(ORDERLYWEB_USER_GROUP,
                    ORDERLYWEB_USER_GROUP_USER,
                    ORDERLYWEB_USER, joinType = JoinType.LEFT_OUTER_JOIN)
            .join(ORDERLYWEB_USER_GROUP_PERMISSION_ALL)
            .on(ORDERLYWEB_USER_GROUP_PERMISSION_ALL.USER_GROUP.eq(ORDERLYWEB_USER_GROUP.ID))
            .where(ORDERLYWEB_USER_GROUP_PERMISSION_ALL.PERMISSION.eq("reports.read"))
            .and(ORDERLYWEB_USER.EMAIL.isNull.or(ORDERLYWEB_USER_GROUP.ID.ne(ORDERLYWEB_USER.EMAIL)))

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
        val roleName = group.key
        val permissions = authRepo.getPermissionsForGroup(roleName)

        return Role(group.key, group.value.mapNotNull { u ->
            if (u[ORDERLYWEB_USER.USERNAME] != null)
            {
                userMapper.mapUser(u)
            }
            else
            {
                null
            }
        }, permissions)
    }

}
