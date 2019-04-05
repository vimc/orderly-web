package org.vaccineimpact.orderlyweb.tests

import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.Config
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.Tables.*
import org.vaccineimpact.orderlyweb.models.ArtefactFormat
import org.vaccineimpact.orderlyweb.models.FilePurpose
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import java.io.File
import java.sql.Timestamp
import kotlin.math.abs
import kotlin.streams.asSequence

data class ChangelogWithPublicVersion
constructor(val reportVersion: String,
            val label: String,
            val value: String,
            val fromFile: Boolean,
            val reportVersionPublic: String? = null)

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
        val rows = it.dsl.select(REPORT.NAME)
                .from(REPORT)
                .where(REPORT.NAME.eq(name))
                .fetch()

        if (rows.isEmpty())
        {

            val reportRecord = it.dsl.newRecord(REPORT)
                    .apply {
                        this.name = name
                    }
            reportRecord.store()
        }

        val reportVersionRecord = it.dsl.newRecord(REPORT_VERSION)
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
        it.dsl.update(REPORT)
                .set(REPORT.LATEST, version)
                .where(REPORT.NAME.eq(name))
                .execute()

    }

}

fun insertChangelog(changelog: List<ChangelogWithPublicVersion>)
{
    JooqContext().use {

        for (entry in changelog)
        {
            //NB we can't use ChangelogRecord to do this insert because this has been generated by Jooq with a
            //non-nullable id property. We want to specify no id, because this is an auto-incrementing field in the db,
            //but this would throw an error in ChangeRecord.
            it.dsl.insertInto(CHANGELOG)
                    .set(CHANGELOG.LABEL, entry.label)
                    .set(CHANGELOG.VALUE, entry.value)
                    .set(CHANGELOG.FROM_FILE, entry.fromFile)
                    .set(CHANGELOG.REPORT_VERSION, entry.reportVersion)
                    .set(CHANGELOG.REPORT_VERSION_PUBLIC, entry.reportVersionPublic)
                    .execute()

        }
    }
}

fun insertArtefact(reportVersionId: String,
                   description: String = "description",
                   format: ArtefactFormat = ArtefactFormat.REPORT,
                   fileNames: List<String>)
{

    JooqContext().use { it ->

        val lastId = it.dsl.select(REPORT_VERSION_ARTEFACT.ID.max())
                .from(REPORT_VERSION_ARTEFACT)
                .fetchAnyInto(Int::class.java)
                ?: 0

        it.dsl.insertInto(REPORT_VERSION_ARTEFACT)
                .set(REPORT_VERSION_ARTEFACT.DESCRIPTION, description)
                .set(REPORT_VERSION_ARTEFACT.FORMAT, format.toString().toLowerCase())
                .set(REPORT_VERSION_ARTEFACT.REPORT_VERSION, reportVersionId)
                .set(REPORT_VERSION_ARTEFACT.ORDER, lastId + 1)
                .set(REPORT_VERSION_ARTEFACT.ID, lastId + 1)
                .execute()

        fileNames.map { f ->
            val hash = generateRandomString()
            it.dsl.insertInto(FILE)
                    .set(FILE.HASH, hash)
                    .set(FILE.SIZE, 1234)
                    .execute()

            it.dsl.insertInto(FILE_ARTEFACT)
                    .set(FILE_ARTEFACT.FILENAME, f)
                    .set(FILE_ARTEFACT.ARTEFACT, lastId + 1)
                    .set(FILE_ARTEFACT.FILE_HASH, hash)
                    .execute()
        }
    }
}

fun insertData(reportVersionId: String,
               name: String,
               sql: String,
               hash: String)
{
    JooqContext().use {
        it.dsl.insertInto(DATA)
                .set(DATA.HASH, hash)
                .set(DATA.SIZE_CSV, 1234)
                .set(DATA.SIZE_RDS, 1234)
                .onDuplicateKeyIgnore()
                .execute()

        it.dsl.insertInto(REPORT_VERSION_DATA)
                .set(REPORT_VERSION_DATA.REPORT_VERSION, reportVersionId)
                .set(REPORT_VERSION_DATA.NAME, name)
                .set(REPORT_VERSION_DATA.SQL, sql)
                .set(REPORT_VERSION_DATA.HASH, hash)
                .execute()
    }
}


fun insertUser(email: String,
               name: String)
{
    JooqContext().use {

        it.dsl.insertInto(ORDERLYWEB_USER)
                .set(ORDERLYWEB_USER.EMAIL, email)
                .set(ORDERLYWEB_USER.USERNAME, name)
                .set(ORDERLYWEB_USER.DISPLAY_NAME, name)
                .set(ORDERLYWEB_USER.USER_SOURCE, "github")
                .onDuplicateKeyIgnore()
                .execute()

        it.dsl.insertInto(ORDERLYWEB_USER_GROUP)
                .set(ORDERLYWEB_USER_GROUP.ID, email)
                .onDuplicateKeyIgnore()
                .execute()

        it.dsl.insertInto(ORDERLYWEB_USER_GROUP_USER)
                .set(ORDERLYWEB_USER_GROUP_USER.EMAIL, email)
                .set(ORDERLYWEB_USER_GROUP_USER.USER_GROUP, email)
                .onDuplicateKeyIgnore()
                .execute()
    }
}

fun giveUserGroupPermission(groupName: String,
                            permissionName: String,
                            scope: Scope)
{
    JooqContext().use {

        val lastId = it.dsl.select(ORDERLYWEB_USER_GROUP_PERMISSION.ID.max())
                .from(ORDERLYWEB_USER_GROUP_PERMISSION)
                .singleOrNull()?.into(Int::class.java) ?: 0


        var abstractId = it.dsl.select(ORDERLYWEB_USER_GROUP_PERMISSION.ID)
                .from(ORDERLYWEB_USER_GROUP_PERMISSION)
                .where(ORDERLYWEB_USER_GROUP_PERMISSION.PERMISSION.eq(permissionName))
                .and(ORDERLYWEB_USER_GROUP_PERMISSION.USER_GROUP.eq(groupName))
                .fetch()
                .singleOrNull()?.into(Int::class.java)

        if (abstractId == null)
        {
            it.dsl.insertInto(ORDERLYWEB_USER_GROUP_PERMISSION)
                    .set(ORDERLYWEB_USER_GROUP_PERMISSION.ID, lastId + 1)
                    .set(ORDERLYWEB_USER_GROUP_PERMISSION.PERMISSION, permissionName)
                    .set(ORDERLYWEB_USER_GROUP_PERMISSION.USER_GROUP, groupName)
                    .onDuplicateKeyIgnore()
                    .execute()
        }

        abstractId = it.dsl.select(ORDERLYWEB_USER_GROUP_PERMISSION.ID)
                .from(ORDERLYWEB_USER_GROUP_PERMISSION)
                .where(ORDERLYWEB_USER_GROUP_PERMISSION.PERMISSION.eq(permissionName))
                .and(ORDERLYWEB_USER_GROUP_PERMISSION.USER_GROUP.eq(groupName))
                .fetchInto(Int::class.java)
                .first()

        if (scope is Scope.Global)
        {
            it.dsl.insertInto(ORDERLYWEB_USER_GROUP_GLOBAL_PERMISSION)
                    .set(ORDERLYWEB_USER_GROUP_GLOBAL_PERMISSION.ID, abstractId)
                    .onDuplicateKeyIgnore()
                    .execute()
        }
        if (scope.databaseScopePrefix == "report")
        {
            it.dsl.insertInto(ORDERLYWEB_USER_GROUP_REPORT_PERMISSION)
                    .set(ORDERLYWEB_USER_GROUP_REPORT_PERMISSION.ID, abstractId)
                    .set(ORDERLYWEB_USER_GROUP_REPORT_PERMISSION.REPORT, scope.databaseScopeId)
                    .onDuplicateKeyIgnore()
                    .execute()
        }
        if (scope.databaseScopePrefix == "version")
        {
            it.dsl.insertInto(ORDERLYWEB_USER_GROUP_VERSION_PERMISSION)
                    .set(ORDERLYWEB_USER_GROUP_VERSION_PERMISSION.ID, abstractId)
                    .set(ORDERLYWEB_USER_GROUP_VERSION_PERMISSION.VERSION, scope.databaseScopeId)
                    .onDuplicateKeyIgnore()
                    .execute()
        }
    }
}

fun generateRandomString(len: Long = 10): String
{
    val source = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    return java.util.Random().ints(len, 0, source.length)
            .asSequence()
            .map(source::get)
            .joinToString("")
}

fun insertFileInput(reportVersion: String, fileName: String, purpose: FilePurpose)
{
    JooqContext().use {

        val hash = generateRandomString()
        it.dsl.insertInto(FILE)
                .set(FILE.HASH, hash)
                .set(FILE.SIZE, 1234)
                .execute()

        it.dsl.insertInto(FILE_INPUT)
                .set(FILE_INPUT.FILE_PURPOSE, purpose.toString())
                .set(FILE_INPUT.FILENAME, fileName)
                .set(FILE_INPUT.FILE_HASH, hash)
                .set(FILE_INPUT.REPORT_VERSION, reportVersion)
                .execute()
    }
}


fun getArchiveFolder(reportName: String, reportVersion: String, config: Config): String
{
    return "${config["orderly.root"]}archive/$reportName/$reportVersion/"
}

fun createArchiveFolder(reportName: String, reportVersion: String, config: Config = AppConfig())
{
    val folderName = getArchiveFolder(reportName, reportVersion, config)
    val folder = File(folderName)
    if (!folder.exists())
    {
        println("creating archive folder $folderName")
        folder.mkdirs()
    }
}

fun deleteArchiveFolder(reportName: String, reportVersion: String, config: Config = AppConfig())
{
    val folderName = getArchiveFolder(reportName, reportVersion, config)
    val folder = File(folderName)
    if (folder.exists())
    {
        val reportFolder = folder.parentFile
        folder.delete()
        if (reportFolder.exists() && reportFolder.list().count() == 0)
        {
            reportFolder.delete()
        }
    }
}