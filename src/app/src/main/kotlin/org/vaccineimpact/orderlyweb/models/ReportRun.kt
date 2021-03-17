package org.vaccineimpact.orderlyweb.models

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.google.gson.JsonParser
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import org.vaccineimpact.orderlyweb.Serializer
import java.time.Instant

data class ReportRun(val name: String, val key: String, val path: String)

data class ReportRunLog(
    val email: String,
    val date: Instant,
    val report: String,
    val instances: Map<String, String>?,
    val params: Map<String, String>?,
    val gitBranch: String?,
    val gitCommit: String?,
    val status: String?,
    val logs: String?,
    val reportVersion: String?
)


private fun String.jsonToStringMap(): Map<String, String>
{
    val element = JsonParser().parse(this)
    val type = TypeToken.getParameterized(Map::class.java, String::class.java, String::class.java).type

    return Serializer.instance.gson.fromJson<Map<String, String>>(element, type)

    //return GsonBuilder().create().toJson(this)
}