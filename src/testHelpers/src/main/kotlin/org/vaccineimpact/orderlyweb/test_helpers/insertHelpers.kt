package org.vaccineimpact.orderlyweb.test_helpers

import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.Tables
import java.sql.Timestamp
import java.time.Instant

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

    db.dsl.newRecord(Tables.ORDERLYWEB_USER_GROUP_USER)
            .apply {
                this.userGroup = email
                this.email = email
            }.insert()
}

fun giveUserGlobalPermission(db: JooqContext, userGroup: String, permissionName: String)
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

