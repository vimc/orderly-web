package org.vaccineimpact.orderlyweb.test_helpers

import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.Tables
import org.vaccineimpact.orderlyweb.db.Tables.ORDERLYWEB_DOCUMENT
import java.sql.Timestamp
import java.time.Instant

fun removePermission(email: String, permissionName: String, scopePrefix: String)
{
    JooqContext().use {

        val permissionID = it.dsl.select(Tables.ORDERLYWEB_USER_GROUP_PERMISSION.ID)
                .from(Tables.ORDERLYWEB_USER_GROUP_PERMISSION)
                .join(Tables.ORDERLYWEB_PERMISSION)
                .on(Tables.ORDERLYWEB_USER_GROUP_PERMISSION.PERMISSION.eq(Tables.ORDERLYWEB_PERMISSION.ID))
                .join(Tables.ORDERLYWEB_USER_GROUP_GLOBAL_PERMISSION)
                .on(Tables.ORDERLYWEB_USER_GROUP_GLOBAL_PERMISSION.ID.eq(Tables.ORDERLYWEB_USER_GROUP_PERMISSION.ID))
                .where(Tables.ORDERLYWEB_USER_GROUP_PERMISSION.USER_GROUP.eq(email))
                .and(Tables.ORDERLYWEB_PERMISSION.ID.eq(permissionName))
                .fetchAny(Tables.ORDERLYWEB_USER_GROUP_PERMISSION.ID)

        if (permissionID != null)
        {

            when (scopePrefix)
            {
                "report" -> it.dsl.deleteFrom(Tables.ORDERLYWEB_USER_GROUP_REPORT_PERMISSION)
                        .where(Tables.ORDERLYWEB_USER_GROUP_REPORT_PERMISSION.ID.eq(permissionID))
                        .execute()
                "version" -> it.dsl.deleteFrom(Tables.ORDERLYWEB_USER_GROUP_VERSION_PERMISSION)
                        .where(Tables.ORDERLYWEB_USER_GROUP_VERSION_PERMISSION.ID.eq(permissionID))
                        .execute()
                else -> it.dsl.deleteFrom(Tables.ORDERLYWEB_USER_GROUP_GLOBAL_PERMISSION)
                        .where(Tables.ORDERLYWEB_USER_GROUP_GLOBAL_PERMISSION.ID.eq(permissionID))
                        .execute()
            }

            it.dsl.deleteFrom(Tables.ORDERLYWEB_USER_GROUP_PERMISSION)
                    .where(Tables.ORDERLYWEB_USER_GROUP_PERMISSION.ID.eq(permissionID))
                    .execute()

        }
    }
}

fun insertReport(name: String,
                 version: String,
                 published: Boolean = true,
                 date: Timestamp = Timestamp(System.currentTimeMillis()),
                 author: String = "author authorson",
                 requester: String = "requester mcfunder")
{

    JooqContext().use {

        val displayname = "display name $name"

        //Does the report already exist in the REPORT table?
        val rows = it.dsl.select(Tables.REPORT.NAME)
                .from(Tables.REPORT)
                .where(Tables.REPORT.NAME.eq(name))
                .fetch()

        if (rows.isEmpty())
        {

            val reportRecord = it.dsl.newRecord(Tables.REPORT)
                    .apply {
                        this.name = name
                    }
            reportRecord.store()
        }

        val reportVersionRecord = it.dsl.newRecord(Tables.REPORT_VERSION)
                .apply {
                    this.id = version
                    this.report = name
                    this.date = date
                    this.displayname = displayname
                    this.description = "description $name"
                    this.requester = requester
                    this.author = author
                    this.published = published
                    this.connection = false
                }
        reportVersionRecord.store()

        //Update latest version of Report
        it.dsl.update(Tables.REPORT)
                .set(Tables.REPORT.LATEST, version)
                .where(Tables.REPORT.NAME.eq(name))
                .execute()

    }

}

fun insertUserAndGroup(db: JooqContext, email: String)
{
    db.dsl.newRecord(Tables.ORDERLYWEB_USER)
            .apply {
                this.username = email
                this.displayName = email
                this.email = email
                this.userSource = "github"
                this.lastLoggedIn = Instant.now().toString()
            }.store()

    db.dsl.newRecord(Tables.ORDERLYWEB_USER_GROUP)
            .apply {
                this.id = email
            }.store()

    addUserToGroup(db, email, email)
}

fun insertRole(db: JooqContext, roleName: String, vararg userEmails: String)
{
    db.dsl.newRecord(Tables.ORDERLYWEB_USER_GROUP)
            .apply {
                this.id = roleName
            }.store()

    for (email in userEmails)
    {
        addUserToGroup(db, email, roleName)
    }
}

fun addUserToGroup(db: JooqContext, email: String, userGroupId: String)
{
    db.dsl.newRecord(Tables.ORDERLYWEB_USER_GROUP_USER)
            .apply {
                this.userGroup = userGroupId
                this.email = email
            }.insert()
}

fun insertGlobalPinnedReport(reportName: String, ordering: Int)
{
    JooqContext().use {
        it.dsl.newRecord(Tables.ORDERLYWEB_PINNED_REPORT_GLOBAL)
                .apply {
                    this.report = reportName
                    this.ordering = ordering
                }.insert()
    }
}

fun giveUserGroupGlobalPermission(db: JooqContext, userGroup: String, permissionName: String)
{

    db.dsl.newRecord(Tables.ORDERLYWEB_USER_GROUP_PERMISSION).apply {
        this.userGroup = userGroup
        this.permission = permissionName
    }.insert()

    val id = db.dsl.select(Tables.ORDERLYWEB_USER_GROUP_PERMISSION.ID)
            .from(Tables.ORDERLYWEB_USER_GROUP_PERMISSION)
            .where(Tables.ORDERLYWEB_USER_GROUP_PERMISSION.PERMISSION.eq(permissionName)
                    .and(Tables.ORDERLYWEB_USER_GROUP_PERMISSION.USER_GROUP.eq(userGroup)))
            .fetchOneInto(Int::class.java)

    db.dsl.newRecord(Tables.ORDERLYWEB_USER_GROUP_GLOBAL_PERMISSION).apply {
        this.id = id
    }.insert()
}
