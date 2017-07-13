package org.vaccineimpact.reporting_api.models

import java.beans.ConstructorProperties

data class OrderlyReport
@ConstructorProperties("id", "name", "views", "data", "artefacts", "resources", "date")
constructor(val id: String, val name: String, val view: String,
                         val data: String, val artefacts: String, val resources: String, val date: String)
