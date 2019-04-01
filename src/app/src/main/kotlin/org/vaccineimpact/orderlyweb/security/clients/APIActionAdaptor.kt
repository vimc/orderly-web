package org.vaccineimpact.orderlyweb.security.clients

import org.pac4j.core.context.HttpConstants
import org.pac4j.sparkjava.DefaultHttpActionAdapter
import org.pac4j.sparkjava.SparkWebContext
import org.vaccineimpact.orderlyweb.ContentTypes
import org.vaccineimpact.orderlyweb.DirectActionContext
import org.vaccineimpact.orderlyweb.Serializer
import org.vaccineimpact.orderlyweb.addDefaultResponseHeaders
import org.vaccineimpact.orderlyweb.errors.MissingRequiredPermissionError
import org.vaccineimpact.orderlyweb.models.ErrorInfo
import org.vaccineimpact.orderlyweb.models.Result
import org.vaccineimpact.orderlyweb.models.ResultStatus
import org.vaccineimpact.orderlyweb.security.authorization.mismatchedURL
import org.vaccineimpact.orderlyweb.security.authorization.missingPermissions

class APIActionAdaptor(clients: List<OrderlyWebTokenCredentialClient>) : DefaultHttpActionAdapter()
{
    private val unauthorizedResponse: String = Serializer.instance.toJson(Result(
            ResultStatus.FAILURE,
            null,
            clients.map {
                it.errorInfo
            }
    ))

    private fun forbiddenResponse(authenticationErrors: List<ErrorInfo>): String = Serializer.instance.toJson(Result(
            ResultStatus.FAILURE,
            null,
            authenticationErrors
    ))


    override fun adapt(code: Int, context: SparkWebContext): Any? = when (code)
    {
        HttpConstants.UNAUTHORIZED ->
        {
            addDefaultResponseHeaders(context.response)
            spark.Spark.halt(code, unauthorizedResponse)
        }
        HttpConstants.FORBIDDEN ->
        {
            addDefaultResponseHeaders(context.response)

            val profile = DirectActionContext(context).userProfile!!

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
