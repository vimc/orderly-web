package org.vaccineimpact.reporting_api.security

import org.pac4j.core.context.HttpConstants
import org.pac4j.sparkjava.DefaultHttpActionAdapter
import org.pac4j.sparkjava.SparkWebContext
import org.vaccineimpact.api.models.ErrorInfo
import org.vaccineimpact.api.models.Result
import org.vaccineimpact.api.models.ResultStatus
import org.vaccineimpact.reporting_api.ContentTypes
import org.vaccineimpact.reporting_api.DirectActionContext
import org.vaccineimpact.reporting_api.Serializer
import org.vaccineimpact.reporting_api.addDefaultResponseHeaders
import org.vaccineimpact.reporting_api.errors.MissingRequiredPermissionError

class TokenActionAdapter(clients: List<MontaguCredentialClientWrapper>) : DefaultHttpActionAdapter()
{
    val unauthorizedResponse: String = Serializer.instance.toJson(Result(
            ResultStatus.FAILURE,
            null,
            clients.map {
                it.errorInfo
            }
    ))

    fun forbiddenResponse(authenticationErrors: List<ErrorInfo>): String = Serializer.instance.toJson(Result(
            ResultStatus.FAILURE,
            null,
            authenticationErrors
    ))


    override fun adapt(code: Int, context: SparkWebContext): Any? = when (code)
    {
        HttpConstants.UNAUTHORIZED ->
        {
            addDefaultResponseHeaders(context.response, ContentTypes.json)
            spark.Spark.halt(code, unauthorizedResponse)
        }
        HttpConstants.FORBIDDEN ->
        {
            addDefaultResponseHeaders(context.response, ContentTypes.json)

            val profile = DirectActionContext(context).userProfile

            val missingUrl = profile.getAttributeOrDefault(MISSING_URL, "")

            val authenticationErrors = mutableListOf<ErrorInfo>()

            if (!missingUrl.isEmpty())
            {
                authenticationErrors.add(ErrorInfo("forbidden", missingUrl))
            }
            val missingPermissions = profile.getAttributeOrDefault(MISSING_PERMISSIONS, mutableSetOf<String>())

            if (missingPermissions.any())
            {
                authenticationErrors.addAll(MissingRequiredPermissionError(missingPermissions).problems)
            }

            spark.Spark.halt(code, forbiddenResponse(authenticationErrors))
        }
        else -> super.adapt(code, context)
    }
}
