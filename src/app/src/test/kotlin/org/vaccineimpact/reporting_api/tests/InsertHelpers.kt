package org.vaccineimpact.reporting_api.tests

import org.vaccineimpact.reporting_api.db.JooqContext
import org.vaccineimpact.reporting_api.db.Tables.ORDERLY
import org.vaccineimpact.reporting_api.db.Tables.REPORT
import org.vaccineimpact.reporting_api.db.Tables.REPORT_VERSION
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
                 requester: String = "requester mcfunder")
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
                        this.name = name,
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

    }
}