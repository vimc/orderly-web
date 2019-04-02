package org.vaccineimpact.orderlyweb.security.authorization

import org.jooq.Record
import org.pac4j.core.authorization.generator.AuthorizationGenerator
import org.pac4j.core.context.WebContext
import org.pac4j.core.profile.CommonProfile
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.Tables.*
import org.vaccineimpact.orderlyweb.db.asTemporaryTable
import org.vaccineimpact.orderlyweb.db.fromJoinPath
import org.vaccineimpact.orderlyweb.db.withTemporaryTable
import org.vaccineimpact.orderlyweb.errors.UnknownObjectError
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.PermissionSet
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission

interface AuthorizationRepository
{
    fun ensureUserGroupHasPermission(userGroup: String, permission: ReifiedPermission)
}

class OrderlyAuthorizationRepository : AuthorizationGenerator<CommonProfile>, AuthorizationRepository
{
    private val ALL_GROUP_PERMISSIONS = "all_group_permissions"
    private val PERMISSION_NAME = "permission_name"
    private val GROUP_PERMISSION_ID = "permission_id"

    override fun generate(context: WebContext?, profile: CommonProfile): CommonProfile
    {
        val permissions = JooqContext().use {

            val abstractPermissions =
                    it.dsl.select(ORDERLYWEB_PERMISSION.ID.`as`(PERMISSION_NAME),
                            ORDERLYWEB_USER_GROUP_PERMISSION.ID.`as`(GROUP_PERMISSION_ID))
                            .fromJoinPath(ORDERLYWEB_PERMISSION,
                                    ORDERLYWEB_USER_GROUP_PERMISSION,
                                    ORDERLYWEB_USER_GROUP,
                                    ORDERLYWEB_USER_GROUP_USER,
                                    ORDERLYWEB_USER)
                            .where(ORDERLYWEB_USER.EMAIL.eq(profile.id))
                            .asTemporaryTable(ALL_GROUP_PERMISSIONS)

            val globalPermissions = mapGlobalPermissions(
                    it.dsl.withTemporaryTable(abstractPermissions)
                            .select(abstractPermissions.field<String>(PERMISSION_NAME))
                            .from(abstractPermissions.tableName)
                            .join(ORDERLYWEB_USER_GROUP_GLOBAL_PERMISSION)
                            .on(ORDERLYWEB_USER_GROUP_GLOBAL_PERMISSION.ID
                                    .eq(abstractPermissions.field(GROUP_PERMISSION_ID)))
                            .fetch()
            )

            val reportPermissions = mapReportPermissions(
                    it.dsl.withTemporaryTable(abstractPermissions)
                            .select(abstractPermissions.field<String>(PERMISSION_NAME),
                                    ORDERLYWEB_USER_GROUP_REPORT_PERMISSION.REPORT)
                            .from(abstractPermissions.tableName)
                            .join(ORDERLYWEB_USER_GROUP_REPORT_PERMISSION)
                            .on(ORDERLYWEB_USER_GROUP_REPORT_PERMISSION.ID
                                    .eq(abstractPermissions.field(GROUP_PERMISSION_ID)))
                            .fetch()
            )

            val versionPermissions = mapVersionPermissions(
                    it.dsl.withTemporaryTable(abstractPermissions)
                            .select(abstractPermissions.field<String>(PERMISSION_NAME),
                                    ORDERLYWEB_USER_GROUP_VERSION_PERMISSION.VERSION)
                            .from(abstractPermissions.tableName)
                            .join(ORDERLYWEB_USER_GROUP_VERSION_PERMISSION)
                            .on(ORDERLYWEB_USER_GROUP_VERSION_PERMISSION.ID
                                    .eq(abstractPermissions.field(GROUP_PERMISSION_ID)))
                            .fetch()
            )

            PermissionSet(globalPermissions + reportPermissions + versionPermissions)
        }

        profile.orderlyWebPermissions = permissions
        return profile
    }

    override fun ensureUserGroupHasPermission(userGroup: String, permission: ReifiedPermission)
    {
        JooqContext().use {
            it.dsl.selectFrom(ORDERLYWEB_PERMISSION)
                    .where(ORDERLYWEB_PERMISSION.ID.eq(permission.name))
                    .singleOrNull() ?: throw UnknownObjectError(permission.name, ReifiedPermission::class)

            val permissionsForGroup = getAllPermissionsForGroup(it, userGroup)

            if (!permissionsForGroup.contains(permission))
            {
                val newUserGroupPermission = it.dsl.newRecord(ORDERLYWEB_USER_GROUP_PERMISSION).apply {
                    this.userGroup = userGroup
                    this.permission = permission.name
                }

                newUserGroupPermission.store()
                val id = newUserGroupPermission[ORDERLYWEB_USER_GROUP_PERMISSION.ID] as Int

                when (permission.scope.databaseScopePrefix)
                {
                    "*" -> it.dsl.newRecord(ORDERLYWEB_USER_GROUP_GLOBAL_PERMISSION).apply {
                        this.id = id
                    }.insert()

                    "report" -> it.dsl.newRecord(ORDERLYWEB_USER_GROUP_REPORT_PERMISSION).apply {
                        this.id = id
                        this.report = permission.scope.databaseScopeId
                    }.insert()

                    "version" -> it.dsl.newRecord(ORDERLYWEB_USER_GROUP_VERSION_PERMISSION).apply {
                        this.id = id
                        this.version = permission.scope.databaseScopeId
                    }.insert()
                }
            }
        }

    }

    private fun getAllPermissionsForGroup(db: JooqContext, userGroup: String): List<ReifiedPermission>
    {

        val abstractPermissions =
                db.dsl.select(ORDERLYWEB_PERMISSION.ID.`as`(PERMISSION_NAME),
                        ORDERLYWEB_USER_GROUP_PERMISSION.ID.`as`(GROUP_PERMISSION_ID))
                        .fromJoinPath(ORDERLYWEB_PERMISSION,
                                ORDERLYWEB_USER_GROUP_PERMISSION)
                        .where(ORDERLYWEB_USER_GROUP.ID.eq(userGroup))
                        .asTemporaryTable(ALL_GROUP_PERMISSIONS)

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