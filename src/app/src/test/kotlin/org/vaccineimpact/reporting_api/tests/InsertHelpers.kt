package org.vaccineimpact.reporting_api.tests

import org.vaccineimpact.api.models.Changelog
import org.vaccineimpact.reporting_api.db.Config
import org.vaccineimpact.reporting_api.db.AppConfig
import org.vaccineimpact.reporting_api.db.JooqContext
import org.vaccineimpact.reporting_api.db.Tables.CHANGELOG
import org.vaccineimpact.reporting_api.db.Tables.CHANGELOG_LABEL
import org.vaccineimpact.reporting_api.db.Tables.ORDERLY
import org.vaccineimpact.reporting_api.db.Tables.REPORT
import org.vaccineimpact.reporting_api.db.Tables.REPORT_VERSION
import java.io.File
import java.sql.Timestamp

fun insertReport(name: String,
                 version: String,
                 views: String = "{\"coverage_info\":\"coverage_info.sql\"}",
                 data: String = "{\"dat\":\"SELECT\\n coverage_info.*,\\n coverage.year,\\n coverage.country,\\n coverage.coverage\\nFROM coverage JOIN coverage_info ON coverage_info.coverage_set = coverage.coverage_set WHERE\\n coverage_info.touchstone = ?touchstone\\n AND coverage_info.disease = ?disease\\n AND country = ?country\\n AND coverage IS NOT NULL\"}",
                 artefacts: String = "[{\"staticgraph\":{\"filenames\":[\"staticgraph.png\"],\"description\":\"A plot of coverage over time\"}}]",
                 hashArtefacts: String = "{\"summary.csv\":\"07dffb00305279935544238b39d7b14b\",\"mygraph.png\":\"4b89e0b767cee1c30f2e910684189680\"}",
                 hashData: String = "{\"dat\": \"62781hjwkjkeq\"}",
                 hashResources: String = "{\"resource.csv\": \"gfe7064mvdfjieync\"}",
                 resources: String = "[\"resource.csv\"]",
                 published: Boolean = true,
                 author: String = "author authorson",
                 requester: String = "requester mcfunder",
                 changelog: List<Changelog> = listOf(Changelog(version,"public","did something great", true),
                                                Changelog(version,"internal","did something awful", false)))
{

    JooqContext().use {

        //Insert report in both old and new schemas

        val date = Timestamp(System.currentTimeMillis())
        val displayname = "display name $name"

        val record = it.dsl.newRecord(ORDERLY)
                .apply {
                    this.name = name
                    this.displayname = displayname
                    this.id = version
                    this.views = views
                    this.data = data
                    this.artefacts = artefacts
                    this.date = date
                    this.hashArtefacts = hashArtefacts
                    this.hashData = hashData
                    this.hashResources = hashResources
                    this.published = published
                    this.resources = resources
                    this.author = author
                    this.requester = requester
                    this.script = "script.R"
                }
        record.store()

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
                        this.latest = version
                    }
            reportRecord.store()
        }
        else
        {
            //Update latest version of Report
            it.dsl.update(REPORT)
                    .set(REPORT.LATEST, version)
                    .where(REPORT.NAME.eq(name))
                    .execute()
        }


        val reportVersionRecord = it.dsl.newRecord(REPORT_VERSION)
                .apply{
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

        //Check if we need to add changelog labels
        val labels = it.dsl.select(CHANGELOG_LABEL.ID)
                .from(CHANGELOG_LABEL)
                .fetch()

        if (labels.isEmpty())
        {
             val publicRecord = it.dsl.newRecord(CHANGELOG_LABEL)
                     .apply{
                         this.id = "public"
                         this.public = true
                     }
             publicRecord.store();

            val internalRecord = it.dsl.newRecord(CHANGELOG_LABEL)
                    .apply{
                        this.id = "internal"
                        this.public = false
                    }
            internalRecord.store();
        }

        for(entry in changelog)
        {
            //NB we can't use ChangelogRecord to do this insert because this has been generated by Jooq with a
            //non-nullable id property. We want to specify no id, because this is an auto-incrementing field in the db,
            //but this would throw an error in ChangeRecord.
            it.dsl.insertInto(CHANGELOG)
                    .set(CHANGELOG.LABEL, entry.label)
                    .set(CHANGELOG.VALUE, entry.value)
                    .set(CHANGELOG.FROM_FILE, entry.fromFile)
                    .set(CHANGELOG.REPORT_VERSION, entry.reportVersion)
                    .execute()

        }

    }

}

fun getArchiveFolder(reportName: String, reportVersion: String, config: Config) : String
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