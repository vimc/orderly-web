package org.vaccineimpact.reporting_api.test_helpers

import org.vaccineimpact.reporting_api.models.OrderlyReport

fun mockReport(name: String,
               version: String,
               views: String = "{\"coverage_info\":\"coverage_info.sql\"}",
               data: String = "{\"dat\":\"SELECT\\n coverage_info.*,\\n coverage.year,\\n coverage.country,\\n coverage.coverage\\nFROM coverage JOIN coverage_info ON coverage_info.coverage_set = coverage.coverage_set WHERE\\n coverage_info.touchstone = ?touchstone\\n AND coverage_info.disease = ?disease\\n AND country = ?country\\n AND coverage IS NOT NULL\"}",
               artefacts: String = "{\"mygraph.png\":{\"format\":\"staticgraph\",\"description\":\"A plot of coverage over time\"}}",
               resources: String = "")
        = OrderlyReport(name, version, views, data, artefacts, resources, "2017-06-05")

