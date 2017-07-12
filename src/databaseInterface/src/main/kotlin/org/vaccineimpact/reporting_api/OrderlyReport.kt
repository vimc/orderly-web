package org.vaccineimpact.reporting_api

import java.beans.ConstructorProperties

data class OrderlyReport
@ConstructorProperties("id", "name", "views", "data", "artefacts", "date")
constructor(val id: String, val name: String, val view: String,
                         val data: String, val artefacts: String, val date: String)
