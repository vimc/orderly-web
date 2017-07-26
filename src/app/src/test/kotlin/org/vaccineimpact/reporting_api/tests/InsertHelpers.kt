package org.vaccineimpact.reporting_api.tests

import org.vaccineimpact.reporting_api.db.JooqContext
import org.vaccineimpact.reporting_api.db.Tables.ORDERLY
import java.sql.Timestamp

fun insertReport(name: String,
                 version: String,
                 views: String = "{\"coverage_info\":\"coverage_info.sql\"}",
                 data: String = "{\"dat\":\"SELECT\\n coverage_info.*,\\n coverage.year,\\n coverage.country,\\n coverage.coverage\\nFROM coverage JOIN coverage_info ON coverage_info.coverage_set = coverage.coverage_set WHERE\\n coverage_info.touchstone = ?touchstone\\n AND coverage_info.disease = ?disease\\n AND country = ?country\\n AND coverage IS NOT NULL\"}",
                 artefacts: String = "{\"mygraph.png\":{\"format\":\"staticgraph\",\"description\":\"A plot of coverage over time\"}}",
                 hashArtefacts: String = "{\"summary.csv\":\"07dffb00305279935544238b39d7b14b\",\"mygraph.png\":\"4b89e0b767cee1c30f2e910684189680\"}",
                 hashData: String = "{\"dat\": \"62781hjwkjkeq\"}",
                 hashResources: String = "{\"resource.csv\": \"gfe7064mvdfjieync\"}",
                 published: Boolean = true) {
    JooqContext().use {

        val record = it.dsl.newRecord(ORDERLY)
                .apply {
                    this.name = name
                    this.id = version
                    this.views = views
                    this.data = data
                    this.artefacts = artefacts
                    this.date = Timestamp(System.currentTimeMillis())
                    this.hashArtefacts = hashArtefacts
                    this.hashData = hashData
                    this.hashResources = hashResources
                    this.published = published
                }
        record.store()
    }
}