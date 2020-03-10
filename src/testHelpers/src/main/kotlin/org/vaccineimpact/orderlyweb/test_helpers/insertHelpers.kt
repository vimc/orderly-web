package org.vaccineimpact.orderlyweb.test_helpers

import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.Tables
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

fun insertCustomFields(customFields: Array<String> = arrayOf("author", "requester"))
{
    JooqContext().use {

        for (customField in customFields)
        {
            val customFieldRecord = it.dsl.newRecord(Tables.CUSTOM_FIELDS)
                    .apply {
                        this.id = customField
                    }
            customFieldRecord.store()
        }
    }
}


private fun getNewCustomFieldId(ctx: JooqContext): Int
{
    val maxFieldId = ctx.dsl.select(Tables.REPORT_VERSION_CUSTOM_FIELDS.ID.max())
            .from(Tables.REPORT_VERSION_CUSTOM_FIELDS)
            .fetchAny()[0] as Int?
    return (maxFieldId ?: 0) + 1
}

private fun getNewParameterId(ctx: JooqContext): Int
{
    val maxFieldId = ctx.dsl.select(Tables.PARAMETERS.ID.max())
            .from(Tables.PARAMETERS)
            .fetchAny()[0] as Int?
    return (maxFieldId ?: 0) + 1
}

private fun getNewReportVersionTagId(ctx: JooqContext): Int
{
    val maxFieldId = ctx.dsl.select(Tables.REPORT_VERSION_TAG.ID.max())
            .from(Tables.REPORT_VERSION_TAG)
            .fetchAny()[0] as Int?
    return (maxFieldId ?: 0) + 1
}

fun insertReportWithCustomFields(name: String,
                                 version: String,
                                 customFields: Map<String, String>,
                                 published: Boolean = true,
                                 date: Timestamp = Timestamp(System.currentTimeMillis()))
{
    insertReportAndVersion(name, version, published, date)
    JooqContext().use {
        for ((key, value) in customFields)
        {
            val fieldRecord = it.dsl.newRecord(Tables.REPORT_VERSION_CUSTOM_FIELDS)
                    .apply {
                        this.id = getNewCustomFieldId(it)
                        this.reportVersion = version
                        this.key = key
                        this.value = value
                    }
            fieldRecord.store()
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
    insertReportAndVersion(name, version, published, date)

    JooqContext().use {
        val authorFieldRecord = it.dsl.newRecord(Tables.REPORT_VERSION_CUSTOM_FIELDS)
                .apply {
                    this.id = getNewCustomFieldId(it)
                    this.reportVersion = version
                    this.key = "author"
                    this.value = author
                }
        authorFieldRecord.store()

        val requesterFieldRecord = it.dsl.newRecord(Tables.REPORT_VERSION_CUSTOM_FIELDS)
                .apply {
                    this.id = getNewCustomFieldId(it)
                    this.reportVersion = version
                    this.key = "requester"
                    this.value = requester
                }
        requesterFieldRecord.store()
    }
}

fun insertVersionParameterValues(version: String,
                                 parameterValues: Map<String, String>)
{
    JooqContext().use {

        val typeExists = it.dsl.selectFrom(Tables.PARAMETERS_TYPE)
                .where(Tables.PARAMETERS_TYPE.NAME.eq("text"))
                .fetch()
                .count() > 0

        if (!typeExists)
        {
            val typeRecord = it.dsl.newRecord(Tables.PARAMETERS_TYPE)
                    .apply{
                        this.name = "text"
                    }
            typeRecord.store()
        }

        for ((k,v) in parameterValues)
        {
            val parameterRecord = it.dsl.newRecord(Tables.PARAMETERS)
                    .apply {
                        this.id = getNewParameterId(it)
                        this.reportVersion = version
                        this.name = k
                        this.type = "text"
                        this.value = v
                    }
            parameterRecord.store()
        }
    }
}

fun insertVersionTags(version: String, tags: List<String>)
{
    JooqContext().use{
        for(tag in tags)
        {
            val tagRecord = it.dsl.newRecord(Tables.ORDERLYWEB_REPORT_VERSION_TAG)
                    .apply{
                        this.reportVersion = version
                        this.tag = tag
                    }
            tagRecord.store()
        }
    }
}

fun insertReportTags(report: String, tags: List<String>)
{
    JooqContext().use{
        for(tag in tags)
        {
            val tagRecord = it.dsl.newRecord(Tables.ORDERLYWEB_REPORT_TAG)
                    .apply{
                        this.report = report
                        this.tag = tag
                    }
            tagRecord.store()
        }
    }
}

fun insertOrderlyTags(version: String, tags: List<String>)
{
    JooqContext().use{
        for(tag in tags)
        {
            addOrderlyTag(tag, it)

            val tagRecord = it.dsl.newRecord(Tables.REPORT_VERSION_TAG)
                    .apply{
                        this.id = getNewReportVersionTagId(it)
                        this.reportVersion = version
                        this.tag = tag
                    }
            tagRecord.store()
        }
    }
}

private fun addOrderlyTag(tag: String, ctx: JooqContext)
{
    val count = ctx.dsl.selectCount()
            .from(Tables.TAG)
            .where(Tables.TAG.ID.eq(tag))
            .fetchOne(0, Int::class.java)
    if (count == 0)
    {
        val tagRecord = ctx.dsl.newRecord(Tables.TAG)
                .apply{
                    this.id = tag
                }
        tagRecord.store()
    }

}

private fun insertReportAndVersion(name: String,
                                   version: String,
                                   published: Boolean,
                                   date: Timestamp)
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
                    this.requester = ""
                    this.author = ""
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

fun insertDocument(db: JooqContext, path: String, isFile: Int, parent: String? = null)
{
    db.dsl.insertInto(Tables.ORDERLYWEB_DOCUMENT)
            .set(Tables.ORDERLYWEB_DOCUMENT.NAME, path)
            .set(Tables.ORDERLYWEB_DOCUMENT.PATH, path)
            .set(Tables.ORDERLYWEB_DOCUMENT.IS_FILE, isFile)
            .set(Tables.ORDERLYWEB_DOCUMENT.SHOW, 1)
            .set(Tables.ORDERLYWEB_DOCUMENT.PARENT, parent)
            .execute()
}