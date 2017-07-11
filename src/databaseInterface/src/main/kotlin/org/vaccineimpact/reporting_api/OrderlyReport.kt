package org.vaccineimpact.reporting_api

import org.json.JSONObject
import java.beans.ConstructorProperties

data class OrderlyReport(val id: String, val name: String, val view: JSONObject,
                         val data: JSONObject, val artefacts: JSONObject, val date: String) {

    @ConstructorProperties("id", "name", "views", "data", "artefacts", "date")
    constructor(id: String, name: String, views: String, data: String, artefacts: String, date: String)
            : this(id, name, JSONObject(views), JSONObject(data), JSONObject(artefacts), date)

}

