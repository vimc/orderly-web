package org.vaccineimpact.reporting_api

import org.pac4j.core.profile.CommonProfile

interface ActionContext
{
    val userProfile: CommonProfile

    fun contentType(): String
    fun queryParams(key: String): String?
    fun params(): Map<String, String>
    fun params(key: String): String
    fun addResponseHeader(key: String, value: String): Unit

}