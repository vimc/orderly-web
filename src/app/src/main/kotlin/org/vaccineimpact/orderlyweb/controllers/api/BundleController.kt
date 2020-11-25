package org.vaccineimpact.orderlyweb.controllers.api

import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.ContentTypes
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.Config
import org.vaccineimpact.orderlyweb.errors.OrderlyServerError

class BundleController(
    context: ActionContext,
    config: Config
) : Controller(context, config) {
    constructor(context: ActionContext) :
        this(
            context,
            AppConfig()
        )

    fun pack(): Boolean {
        context.getSparkResponse().raw().contentType = "application/zip"

        val name = context.params(":name") // TODO check this is provided

        val client = OkHttpClient()
        val url = appConfig["orderly.server"] + "/v1/bundle/pack/$name" + (if (context.queryString() != null) "?" + context.queryString() else "")
        val json = Gson().toJson(context.postData<String>()) // TODO check json is passed through
        val request = Request.Builder()
            .url(url)
            .post(json.toRequestBody("application/json".toMediaType()))
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw OrderlyServerError(url, response.code)
            context.getSparkResponse().raw().outputStream.write(response.body!!.bytes()) // TODO stream?
        }
        return true
    }

    fun import(): String {
        context.addDefaultResponseHeaders(ContentTypes.json)

        val client = OkHttpClient()
        val url = appConfig["orderly.server"] + "/v1/bundle/import"
        val request = Request.Builder()
            .url(url)
            .post(context.getRequestBodyAsBytes().toRequestBody("application/octet-stream".toMediaType())) // TODO stream?
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw OrderlyServerError(url, response.code)
            return response.body!!.string()
        }
    }
}
