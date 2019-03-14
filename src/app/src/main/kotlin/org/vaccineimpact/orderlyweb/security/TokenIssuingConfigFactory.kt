package org.vaccineimpact.orderlyweb.security

import org.pac4j.core.config.Config
import org.pac4j.core.config.ConfigFactory
import org.pac4j.core.context.HttpConstants
import org.pac4j.http.client.direct.DirectBasicAuthClient
import org.pac4j.sparkjava.DefaultHttpActionAdapter
import org.pac4j.sparkjava.SparkWebContext
import org.vaccineimpact.orderlyweb.Serializer
import org.vaccineimpact.orderlyweb.addDefaultResponseHeaders
import org.vaccineimpact.orderlyweb.errors.FailedLoginError
import spark.Spark as spk

class TokenIssuingConfigFactory(private val serializer: Serializer = Serializer.instance)
    : ConfigFactory
{
    override fun build(vararg parameters: Any?): Config
    {
        val authClient = DirectBasicAuthClient(GithubAuthenticator())
        return Config(authClient).apply {
            httpActionAdapter = BasicAuthActionAdapter(serializer)
            addMethodMatchers()
        }
    }
}

class BasicAuthActionAdapter(serializer: Serializer)
    : DefaultHttpActionAdapter()
{
    val unauthorizedResponse: String = serializer.gson.toJson(
            FailedLoginError("invalid_client").asResult())

    override fun adapt(code: Int, context: SparkWebContext): Any? = when (code)
    {
        HttpConstants.UNAUTHORIZED ->
        {
            context.response.addHeader("x-WWW-Authenticate", "Basic")
            haltWithError(code, context)
        }
        else -> super.adapt(code, context)
    }

    private fun haltWithError(code: Int, context: SparkWebContext)
    {
        addDefaultResponseHeaders(context.sparkRequest, context.response)
        spark.Spark.halt(code, unauthorizedResponse)
    }
}
