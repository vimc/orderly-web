package org.vaccineimpact.orderlyweb.models.orderly_server

data class OrderlyServerReportRun(val params: Map<String, String>, val changelog: OrderlyServerChangelog?)
data class OrderlyServerChangelog(val message: String, val type: String)

