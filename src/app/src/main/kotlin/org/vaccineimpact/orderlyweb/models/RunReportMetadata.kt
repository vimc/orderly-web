package org.vaccineimpact.orderlyweb.models

data class RunReportMetadata(
        val instancesSupported: Boolean,
        val gitSupported: Boolean,
        val instances: List<String>?,
        val changelogTypes: List<String>)