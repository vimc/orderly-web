package org.vaccineimpact.reporting_api.test_helpers

import org.vaccineimpact.reporting_api.db.Config
import org.vaccineimpact.reporting_api.db.JooqContext
import org.vaccineimpact.reporting_api.db.Tables.*
import java.io.File
import java.sql.Timestamp

fun insertReport(name: String,
                 version: String,
                 views: String = "test_view.sql",
                 data: String = "{\"dat\":\"SELECT\\n coverage_info.*,\\n coverage.year,\\n coverage.country,\\n coverage.coverage\\nFROM coverage JOIN coverage_info ON coverage_info.coverage_set = coverage.coverage_set WHERE\\n coverage_info.touchstone = ?touchstone\\n AND coverage_info.disease = ?disease\\n AND country = ?country\\n AND coverage IS NOT NULL\"}",
                 artefacts: String = "{\"mygraph.png\":{\"format\":\"staticgraph\",\"description\":\"A plot of coverage over time\"}}")
{
    JooqContext(File(Config["dbTest.location"]).absolutePath).use {

        val record = it.dsl.newRecord(ORDERLY)
                .apply{
                    this.name = name
                    this.id = version
                    this.views = views
                    this.data = data
                    this.artefacts = artefacts
                    this.date = Timestamp(System.currentTimeMillis())
                }
        record.store()
    }
}