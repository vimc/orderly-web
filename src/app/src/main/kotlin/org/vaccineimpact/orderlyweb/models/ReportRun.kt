package org.vaccineimpact.orderlyweb.models

import java.time.Instant

data class ReportRun(val name: String, val key: String, val path: String)

data class RunningReports(val date: Instant, val name: String, val key: String)
