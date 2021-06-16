package org.vaccineimpact.orderlyweb.models

data class RunReportMetadata(
        val instancesSupported: Boolean,
        val gitSupported: Boolean,
        val instances: Map<String, List<String>>,
        val changelogTypes: List<String>)
