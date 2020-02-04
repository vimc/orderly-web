package org.vaccineimpact.orderlyweb.db

import org.jooq.Record
import org.jooq.Record3
import org.jooq.SelectConditionStep
import org.vaccineimpact.orderlyweb.db.Tables.*
import org.vaccineimpact.orderlyweb.errors.DuplicateKeyError
import org.vaccineimpact.orderlyweb.errors.UnknownObjectError
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.User
import org.vaccineimpact.orderlyweb.models.permissions.PermissionSet
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.models.permissions.Role
import org.jooq.impl.DSL
import org.jooq.DSLContext



interface AuthorizationRepository
{
    fun createUserGroup(userGroup: String)
    fun deleteUserGroup(userGroup: String)
    fun ensureGroupHasMember(userGroup: String, email: String)
    fun ensureGroupDoesNotHaveMember(userGroup: String, email: String)
    fun ensureUserGroupHasPermission(userGroup: String, permission: ReifiedPermission)
    fun ensureUserGroupDoesNotHavePermission(userGroup: String, permission: ReifiedPermission)
    fun getPermissionsForUser(email: String): PermissionSet
    fun getPermissionsForGroup(userGroup: String): List<ReifiedPermission>
    fun getDirectPermissionsForUser(email: String): PermissionSet
    fun getPermissionNames(): List<String>
}

class OrderlyAuthorizationRepository(private val permissionMapper: PermissionMapper = PermissionMapper()) : AuthorizationRepository
{
    override fun getPermissionNames(): List<String>
    {
        return JooqContext().use {
            it.dsl.select(ORDERLYWEB_PERMISSION.ID)
                    .from(ORDERLYWEB_PERMISSION)
                    .fetchInto(String::class.java)
        }
    }

    override fun createUserGroup(userGroup: String)
    {
        JooqContext().use {

            if (it.dsl.selectFrom(ORDERLYWEB_USER_GROUP)
                            .where(Tables.ORDERLYWEB_USER_GROUP.ID.eq(userGroup))
                            .singleOrNull() != null)
            {
                throw DuplicateKeyError(mapOf("user-group" to userGroup))
            }

            it.dsl.newRecord(ORDERLYWEB_USER_GROUP)
                    .apply {
                        this.id = userGroup
                    }.store()
        }
    }

    override fun deleteUserGroup(userGroup: String)
    {
        JooqContext().use {
            it.dsl.transaction{ _ ->

                val userGroupPermissionIds =
                        it.dsl.select(ORDERLYWEB_USER_GROUP_PERMISSION.ID)
                                .from(ORDERLYWEB_USER_GROUP_PERMISSION)
                                .where(ORDERLYWEB_USER_GROUP_PERMISSION.USER_GROUP.eq(userGroup))
                                .fetch(ORDERLYWEB_USER_GROUP_PERMISSION.ID)

                it.dsl.deleteFrom(ORDERLYWEB_USER_GROUP_GLOBAL_PERMISSION)
                        .where(ORDERLYWEB_USER_GROUP_GLOBAL_PERMISSION.ID.`in`(userGroupPermissionIds))

                it.dsl.deleteFrom(ORDERLYWEB_USER_GROUP_REPORT_PERMISSION)
                        .where(ORDERLYWEB_USER_GROUP_REPORT_PERMISSION.ID.`in`(userGroupPermissionIds))

                it.dsl.deleteFrom(ORDERLYWEB_USER_GROUP_VERSION_PERMISSION)
                        .where(ORDERLYWEB_USER_GROUP_VERSION_PERMISSION.ID.`in`(userGroupPermissionIds))

                it.dsl.deleteFrom(ORDERLYWEB_USER_GROUP_PERMISSION)
                        .where(ORDERLYWEB_USER_GROUP_PERMISSION.ID.`in`(userGroupPermissionIds))

                it.dsl.deleteFrom(ORDERLYWEB_USER_GROUP_USER)
                        .where(ORDERLYWEB_USER_GROUP_USER.USER_GROUP.eq(userGroup))

                it.dsl.deleteFrom(ORDERLYWEB_USER_GROUP)
                        .where(ORDERLYWEB_USER_GROUP.ID.eq(userGroup))
            }
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

    override fun ensureGroupDoesNotHaveMember(userGroup: String, email: String)
    {
        JooqContext().use {
            it.dsl.deleteFrom(ORDERLYWEB_USER_GROUP_USER)
                    .where(ORDERLYWEB_USER_GROUP_USER.USER_GROUP.eq(userGroup)
                            .and(ORDERLYWEB_USER_GROUP_USER.EMAIL.eq(email)))
                    .execute()
        }
    }

    override fun getPermissionsForUser(email: String): PermissionSet
    {
        JooqContext().use {
            val perms = getAllPermissionsForUserQuery(it, email)
                    .fetch()
                    .map(permissionMapper::mapPermission)

            return PermissionSet(perms)
        }
    }

    override fun getDirectPermissionsForUser(email: String): PermissionSet
    {
        JooqContext().use {
            val perms = getAllPermissionsForUserQuery(it, email)
                    .and(ORDERLYWEB_USER.EMAIL.eq(ORDERLYWEB_USER_GROUP.ID))
                    .fetch()
                    .map(permissionMapper::mapPermission)

            return PermissionSet(perms)
        }
    }

    override fun ensureUserGroupHasPermission(userGroup: String, permission: ReifiedPermission)
    {
        JooqContext().use {

            checkPermissionExists(it, permission.name)
            checkUserGroupExists(it, userGroup)

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

    override fun ensureUserGroupDoesNotHavePermission(userGroup: String, permission: ReifiedPermission)
    {
        JooqContext().use {
            checkPermissionExists(it, permission.name)
            checkUserGroupExists(it, userGroup)

            val allPermissionRecordsForGroup = getAllPermissionRecordsForGroup(it, userGroup)
            val permissionRecordToDelete = allPermissionRecordsForGroup.firstOrNull {
                permissionMapper.mapPermission(it) == permission }
            if (permissionRecordToDelete != null)
            {
                val idToDelete = permissionRecordToDelete[ORDERLYWEB_USER_GROUP_PERMISSION_ALL.ID]
                when (permission.scope)
                {
                    is Scope.Global ->
                        it.dsl.deleteFrom(ORDERLYWEB_USER_GROUP_GLOBAL_PERMISSION)
                                .where(ORDERLYWEB_USER_GROUP_GLOBAL_PERMISSION.ID.eq(idToDelete))
                                .execute()

                    is Scope.Specific ->
                        when
                        {
                            permission.scope.databaseScopePrefix == "report" ->
                                it.dsl.deleteFrom(ORDERLYWEB_USER_GROUP_REPORT_PERMISSION)
                                        .where(ORDERLYWEB_USER_GROUP_REPORT_PERMISSION.ID.eq(idToDelete))
                                        .and(ORDERLYWEB_USER_GROUP_REPORT_PERMISSION.REPORT.eq(permission.scope.databaseScopeId))
                                        .execute()
                            permission.scope.databaseScopePrefix == "version" ->
                                it.dsl.deleteFrom(ORDERLYWEB_USER_GROUP_VERSION_PERMISSION)
                                        .where(ORDERLYWEB_USER_GROUP_VERSION_PERMISSION.ID.eq(idToDelete))
                                        .and(ORDERLYWEB_USER_GROUP_VERSION_PERMISSION.VERSION.eq(permission.scope.databaseScopeId))
                                        .execute()
                            else -> throw UnknownObjectError(permission.scope.databaseScopePrefix, "permission-scope")
                        }
                }

                //check if the abstract permission should be deleted too - was there only one of these permissions for the user group?
                val thisPermCount = allPermissionRecordsForGroup.count {
                    it[ORDERLYWEB_USER_GROUP_PERMISSION_ALL.PERMISSION] == permission.name
                }

                if (thisPermCount == 1)
                {
                    it.dsl.deleteFrom(ORDERLYWEB_USER_GROUP_PERMISSION)
                            .where(ORDERLYWEB_USER_GROUP_PERMISSION.ID.eq(idToDelete))
                            .execute()
                }
            }

        }
    }

    override fun getPermissionsForGroup(userGroup: String): List<ReifiedPermission>
    {
        JooqContext().use {
            return getAllPermissionsForGroup(it, userGroup)
        }
    }

    private fun checkPermissionExists(db: JooqContext, permission: String)
    {
        db.dsl.selectFrom(ORDERLYWEB_PERMISSION)
                .where(ORDERLYWEB_PERMISSION.ID.eq(permission))
                .singleOrNull() ?: throw UnknownObjectError(permission, "permission")
    }

    private fun checkUserGroupExists(db: JooqContext, userGroup: String)
    {
        db.dsl.selectFrom(ORDERLYWEB_USER_GROUP)
                .where(ORDERLYWEB_USER_GROUP.ID.eq(userGroup))
                .singleOrNull() ?: throw UnknownObjectError(userGroup, "user-group")
    }

    private fun getAllPermissionRecordsForGroup(db: JooqContext, userGroup: String): List<Record>
    {
        return db.dsl.select(ORDERLYWEB_USER_GROUP_PERMISSION_ALL.ID,
                ORDERLYWEB_USER_GROUP_PERMISSION_ALL.PERMISSION,
                ORDERLYWEB_USER_GROUP_PERMISSION_ALL.SCOPE_PREFIX,
                ORDERLYWEB_USER_GROUP_PERMISSION_ALL.SCOPE_ID)
                .from(ORDERLYWEB_USER_GROUP_PERMISSION_ALL)
                .where(ORDERLYWEB_USER_GROUP_PERMISSION_ALL.USER_GROUP.eq(userGroup))
                .fetch()
    }

    private fun getAllPermissionsForGroup(db: JooqContext, userGroup: String): List<ReifiedPermission>
    {
        return getAllPermissionRecordsForGroup(db, userGroup)
                .map(permissionMapper::mapPermission)
    }

    private fun getAllPermissionsForUserQuery(db: JooqContext, email: String): SelectConditionStep<Record3<String, Any, String>>
    {
        return db.dsl.select(ORDERLYWEB_USER_GROUP_PERMISSION_ALL.PERMISSION,
                ORDERLYWEB_USER_GROUP_PERMISSION_ALL.SCOPE_PREFIX,
                ORDERLYWEB_USER_GROUP_PERMISSION_ALL.SCOPE_ID)
                .fromJoinPath(ORDERLYWEB_USER_GROUP,
                        ORDERLYWEB_USER_GROUP_USER,
                        ORDERLYWEB_USER)

                .join(ORDERLYWEB_USER_GROUP_PERMISSION_ALL)
                .on(ORDERLYWEB_USER_GROUP_PERMISSION_ALL.USER_GROUP.eq(ORDERLYWEB_USER_GROUP.ID))
                .where(ORDERLYWEB_USER.EMAIL.eq(email))
    }


}