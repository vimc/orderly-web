package org.vaccineimpact.orderlyweb.controllers.api

import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.Config
import org.vaccineimpact.orderlyweb.errors.OrderlyServerError

class BundleController(
    context: ActionContext,
    config: Config,
    private val httpClient: OkHttpClient
) : Controller(context, config) {
    constructor(context: ActionContext) :
        this(
            context,
            AppConfig(),
            OkHttpClient()
        )

    fun pack(): Boolean {
        val name = context.params(":name") // TODO check this is provided

        val url = appConfig["orderly.server"] + "/v1/bundle/pack/$name" + (if (context.queryString() != null) "?" + context.queryString() else "")
        val json = Gson().toJson(context.postData<String>()) // TODO check json is passed through
        val request = Request.Builder()
            .url(url)
            .post(json.toRequestBody("application/json".toMediaType()))
            .build()
        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw OrderlyServerError(url, response.code)
            val servletResponse = context.getSparkResponse().raw()
            servletResponse.contentType = "application/zip"
            servletResponse.outputStream.write(response.body!!.bytes()) // TODO stream?
        }
        return true
    }

    fun import(): String {
        val url = appConfig["orderly.server"] + "/v1/bundle/import"
        val request = Request.Builder()
            .url(url)
            .post(context.getRequestBodyAsBytes().toRequestBody("application/octet-stream".toMediaType())) // TODO stream?
            .build()
        httpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw OrderlyServerError(url, response.code)
            return response.body!!.string()
        }
    }
}
