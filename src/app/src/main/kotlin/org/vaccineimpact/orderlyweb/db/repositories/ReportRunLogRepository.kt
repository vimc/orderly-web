package org.vaccineimpact.orderlyweb.db.repositories

import org.vaccineimpact.orderlyweb.errors.UnknownObjectError
import org.vaccineimpact.orderlyweb.models.ReportRunLog
import java.time.Instant

interface ReportRunLogRepository
{
    @Throws(UnknownObjectError::class)
    fun getReportRun(key: String): ReportRunLog

    fun updateReportRun(
        key: String,
        status: String,
        version: String?,
        logs: List<String>?,
        startTime: Instant? = Instant.now()
    )
}
