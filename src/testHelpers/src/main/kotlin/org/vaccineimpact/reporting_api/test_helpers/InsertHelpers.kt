package org.vaccineimpact.reporting_api.test_helpers

import org.vaccineimpact.reporting_api.db.JooqContext
import org.vaccineimpact.reporting_api.db.Tables.*

fun JooqContext.addReport(id: String, name: String = id, data: String, artefacts: String, parameters: String? = null)
{
    this.dsl.newRecord(ORDERLY).apply {
        this.id = id
        this.name = name
        this.data = data
        this.artefacts = artefacts
        this.parameters = parameters
    }.store()
}