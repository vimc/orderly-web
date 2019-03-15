package org.vaccineimpact.orderlyweb.security

import org.pac4j.core.context.HttpConstants
import org.pac4j.sparkjava.DefaultHttpActionAdapter
import org.pac4j.sparkjava.SparkWebContext
import org.vaccineimpact.orderlyweb.models.ErrorInfo
import org.vaccineimpact.orderlyweb.models.Result
import org.vaccineimpact.orderlyweb.models.ResultStatus
import org.vaccineimpact.orderlyweb.ContentTypes
import org.vaccineimpact.orderlyweb.DirectActionContext
import org.vaccineimpact.orderlyweb.Serializer
import org.vaccineimpact.orderlyweb.addDefaultResponseHeaders
import org.vaccineimpact.orderlyweb.errors.MissingRequiredPermissionError

class OrderlyActionAdaptor(clients: List<CredentialClientWrapper>)
    : DefaultHttpActionAdapter()
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

            val mismatchedURL = profile.mismatchedURL

            val authenticationErrors = mutableListOf<ErrorInfo>()

            if (mismatchedURL != null)
            {
                authenticationErrors.add(ErrorInfo("forbidden", mismatchedURL))
            }
            val missingPermissions = profile.missingPermissions

            if (missingPermissions.any())
            {
                authenticationErrors.addAll(MissingRequiredPermissionError(missingPermissions).problems)
            }

            spark.Spark.halt(code, forbiddenResponse(authenticationErrors))
        }
        else -> super.adapt(code, context)
    }
}
