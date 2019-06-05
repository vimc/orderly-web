package org.vaccineimpact.orderlyweb.db

import org.jooq.Record
import org.vaccineimpact.orderlyweb.db.Tables.*
import org.vaccineimpact.orderlyweb.errors.DuplicateKeyError
import org.vaccineimpact.orderlyweb.errors.UnknownObjectError
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.User
import org.vaccineimpact.orderlyweb.models.permissions.PermissionSet
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission

interface AuthorizationRepository
{
    fun createUserGroup(userGroup: String)
    fun ensureGroupHasMember(userGroup: String, email: String)
    fun ensureUserGroupHasPermission(userGroup: String, permission: ReifiedPermission)
    fun getPermissionsForUser(email: String): PermissionSet
    fun getReportReaders(reportName: String): Map<User, Scope>
}

class OrderlyAuthorizationRepository : AuthorizationRepository
{
    private val ALL_GROUP_PERMISSIONS = "all_group_permissions"
    private val PERMISSION_NAME = "permission_name"
    private val GROUP_PERMISSION_ID = "permission_id"

    override fun createUserGroup(userGroup: String)
    {
        JooqContext().use {

            if (it.dsl.selectFrom(Tables.ORDERLYWEB_USER_GROUP)
                            .where(Tables.ORDERLYWEB_USER_GROUP.ID.eq(userGroup))
                            .singleOrNull() != null)
            {
                throw DuplicateKeyError(mapOf("user-group" to userGroup))
            }

            it.dsl.newRecord(Tables.ORDERLYWEB_USER_GROUP)
                    .apply {
                        this.id = userGroup
                    }.store()
        }
    }

    override fun ensureGroupHasMember(userGroup: String, email: String)
    {
        JooqContext().use {
            it.dsl.selectFrom(ORDERLYWEB_USER_GROUP)
                    .where(ORDERLYWEB_USER_GROUP.ID.eq(userGroup))
                    .singleOrNull() ?: throw UnknownObjectError(userGroup, "user-group")

            it.dsl.selectFrom(ORDERLYWEB_USER)
                    .where(ORDERLYWEB_USER.EMAIL.eq(email))
                    .singleOrNull() ?: throw UnknownObjectError(email, User::class)

            val membership = it.dsl.selectFrom(ORDERLYWEB_USER_GROUP_USER)
                    .where(ORDERLYWEB_USER_GROUP_USER.USER_GROUP.eq(userGroup)
                            .and(ORDERLYWEB_USER_GROUP_USER.EMAIL.eq(email))).singleOrNull()

            if (membership == null)
            {
                it.dsl.newRecord(ORDERLYWEB_USER_GROUP_USER)
                        .apply {
                            this.email = email
                            this.userGroup = userGroup
                        }.insert()
            }
        }
    }

    override fun getPermissionsForUser(email: String): PermissionSet
    {
        return JooqContext().use {

            val abstractPermissions =
                    it.dsl.select(ORDERLYWEB_PERMISSION.ID.`as`(PERMISSION_NAME),
                            ORDERLYWEB_USER_GROUP_PERMISSION.ID.`as`(GROUP_PERMISSION_ID))
                            .fromJoinPath(ORDERLYWEB_PERMISSION,
                                    ORDERLYWEB_USER_GROUP_PERMISSION,
                                    ORDERLYWEB_USER_GROUP,
                                    ORDERLYWEB_USER_GROUP_USER,
                                    ORDERLYWEB_USER)
                            .where(ORDERLYWEB_USER.EMAIL.eq(email))
                            .asTemporaryTable(ALL_GROUP_PERMISSIONS)

            val allPermissions = getAllPermissions(it, abstractPermissions)
            PermissionSet(allPermissions)
        }
    }

    override fun ensureUserGroupHasPermission(userGroup: String, permission: ReifiedPermission)
    {
        JooqContext().use {
            it.dsl.selectFrom(ORDERLYWEB_PERMISSION)
                    .where(ORDERLYWEB_PERMISSION.ID.eq(permission.name))
                    .singleOrNull() ?: throw UnknownObjectError(permission.name, "permission")

            it.dsl.selectFrom(ORDERLYWEB_USER_GROUP)
                    .where(ORDERLYWEB_USER_GROUP.ID.eq(userGroup))
                    .singleOrNull() ?: throw UnknownObjectError(userGroup, "user-group")

            val permissionsForGroup = getAllPermissionsForGroup(it, userGroup)

            if (!permissionsForGroup.any { p -> p.name == permission.name })
            {
                it.dsl.newRecord(ORDERLYWEB_USER_GROUP_PERMISSION).apply {
                    this.userGroup = userGroup
                    this.permission = permission.name
                }.insert()
            }
            if (!permissionsForGroup.contains(permission))
            {
                val id = it.dsl.select(ORDERLYWEB_USER_GROUP_PERMISSION.ID)
                        .from(ORDERLYWEB_USER_GROUP_PERMISSION)
                        .where(ORDERLYWEB_USER_GROUP_PERMISSION.PERMISSION.eq(permission.name)
                                .and(ORDERLYWEB_USER_GROUP_PERMISSION.USER_GROUP.eq(userGroup)))
                        .fetchOneInto(Int::class.java)

                when (permission.scope)
                {
                    is Scope.Global -> it.dsl.newRecord(ORDERLYWEB_USER_GROUP_GLOBAL_PERMISSION).apply {
                        this.id = id
                    }.insert()

                    is Scope.Specific ->
                        when
                        {
                            permission.scope.databaseScopePrefix == "report" -> it.dsl.newRecord(ORDERLYWEB_USER_GROUP_REPORT_PERMISSION).apply {
                                this.id = id
                                this.report = permission.scope.databaseScopeId
                            }.insert()
                            permission.scope.databaseScopePrefix == "version" -> it.dsl.newRecord(ORDERLYWEB_USER_GROUP_VERSION_PERMISSION).apply {
                                this.id = id
                                this.version = permission.scope.databaseScopeId
                            }.insert()
                            else -> throw UnknownObjectError(permission.scope.databaseScopePrefix, "permission-scope")
                        }
                }
            }
        }

    }

    override fun getReportReaders(reportName: String): Map<User, Scope>
    {
        //Returns all users which can read the report, along with their report read scope (global or report-specific)
        JooqContext().use {
            val result = it.dsl.select(ORDERLYWEB_USER.USERNAME,
                    ORDERLYWEB_USER.DISPLAY_NAME,
                    ORDERLYWEB_USER.EMAIL,
                    ORDERLYWEB_USER.USER_SOURCE,
                    ORDERLYWEB_USER.LAST_LOGGED_IN,
                    ORDERLYWEB_USER_GROUP_PERMISSION_ALL.SCOPE_PREFIX,
                    ORDERLYWEB_USER_GROUP_PERMISSION_ALL.SCOPE_ID)

                    .from(ORDERLYWEB_USER)
                    .join(ORDERLYWEB_USER_GROUP)
                    .on(ORDERLYWEB_USER.EMAIL.eq(ORDERLYWEB_USER_GROUP.ID))

                    .join(ORDERLYWEB_USER_GROUP_PERMISSION_ALL)
                    .on(ORDERLYWEB_USER_GROUP_PERMISSION_ALL.USER_GROUP.eq(ORDERLYWEB_USER_GROUP.ID))

                    .where(ORDERLYWEB_USER_GROUP_PERMISSION_ALL.PERMISSION.eq("reports.read"))
                    .and(permissionIsGlobal().or(permissionIsScopedToReport(reportName)))

                    .orderBy(ORDERLYWEB_USER_GROUP_PERMISSION_ALL.SCOPE_PREFIX.desc())
                    .fetch()

            //associateBy chooses the last value for each key, so should get the global perm if user has both global and report
            return result.associateBy(
                    { it.into(User::class.java) },
                    { if (it[ORDERLYWEB_USER_GROUP_PERMISSION_ALL.SCOPE_PREFIX] == "*")
                        Scope.Global()
                      else
                        Scope.Specific("report", reportName) }
            )

        }
    }

    private fun permissionIsGlobal() = ORDERLYWEB_USER_GROUP_PERMISSION_ALL.SCOPE_PREFIX.eq("*")
    private fun permissionIsScopedToReport(report: String) =
        ORDERLYWEB_USER_GROUP_PERMISSION_ALL.SCOPE_PREFIX.eq("report").
            and(ORDERLYWEB_USER_GROUP_PERMISSION_ALL.SCOPE_ID.eq(report))

    private fun getAllPermissionsForGroup(db: JooqContext, userGroup: String): List<ReifiedPermission>
    {
        val abstractPermissions =
                db.dsl.select(ORDERLYWEB_PERMISSION.ID.`as`(PERMISSION_NAME),
                        ORDERLYWEB_USER_GROUP_PERMISSION.ID.`as`(GROUP_PERMISSION_ID))
                        .fromJoinPath(ORDERLYWEB_PERMISSION,
                                ORDERLYWEB_USER_GROUP_PERMISSION)
                        .where(ORDERLYWEB_USER_GROUP_PERMISSION.USER_GROUP.eq(userGroup))
                        .asTemporaryTable(ALL_GROUP_PERMISSIONS)

        return getAllPermissions(db, abstractPermissions)
    }

    private fun getAllPermissions(db: JooqContext, abstractPermissions: TempTable): List<ReifiedPermission>
    {
        val globalPermissions = mapGlobalPermissions(
                db.dsl.withTemporaryTable(abstractPermissions)
                        .select(abstractPermissions.field<String>(PERMISSION_NAME))
                        .from(abstractPermissions.tableName)
                        .join(ORDERLYWEB_USER_GROUP_GLOBAL_PERMISSION)
                        .on(ORDERLYWEB_USER_GROUP_GLOBAL_PERMISSION.ID
                                .eq(abstractPermissions.field(GROUP_PERMISSION_ID)))
                        .fetch()
        )

        val reportPermissions = mapReportPermissions(
                db.dsl.withTemporaryTable(abstractPermissions)
                        .select(abstractPermissions.field<String>(PERMISSION_NAME),
                                ORDERLYWEB_USER_GROUP_REPORT_PERMISSION.REPORT)
                        .from(abstractPermissions.tableName)
                        .join(ORDERLYWEB_USER_GROUP_REPORT_PERMISSION)
                        .on(ORDERLYWEB_USER_GROUP_REPORT_PERMISSION.ID
                                .eq(abstractPermissions.field(GROUP_PERMISSION_ID)))
                        .fetch()
        )

        val versionPermissions = mapVersionPermissions(
                db.dsl.withTemporaryTable(abstractPermissions)
                        .select(abstractPermissions.field<String>(PERMISSION_NAME),
                                ORDERLYWEB_USER_GROUP_VERSION_PERMISSION.VERSION)
                        .from(abstractPermissions.tableName)
                        .join(ORDERLYWEB_USER_GROUP_VERSION_PERMISSION)
                        .on(ORDERLYWEB_USER_GROUP_VERSION_PERMISSION.ID
                                .eq(abstractPermissions.field(GROUP_PERMISSION_ID)))
                        .fetch()
        )

        return globalPermissions + reportPermissions + versionPermissions
    }

    private fun mapGlobalPermissions(permissionNames: List<Record>): List<ReifiedPermission>
    {
        return permissionNames.map {
            ReifiedPermission(it[PERMISSION_NAME].toString(), Scope.Global())
        }
    }

    private fun mapReportPermissions(permissions: List<Record>): List<ReifiedPermission>
    {
        return permissions.map {
            ReifiedPermission(it[PERMISSION_NAME].toString(),
                    Scope.Specific("report", it[ORDERLYWEB_USER_GROUP_REPORT_PERMISSION.REPORT]))
        }
    }

    private fun mapVersionPermissions(permissions: List<Record>): List<ReifiedPermission>
    {
        return permissions.map {
            ReifiedPermission(it[PERMISSION_NAME].toString(),
                    Scope.Specific("version", it[ORDERLYWEB_USER_GROUP_VERSION_PERMISSION.VERSION]))
        }
    }

}