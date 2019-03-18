package org.vaccineimpact.orderlyweb.security

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

class TokenActionAdapter(clients: List<Client>) : DefaultHttpActionAdapter()
{
    private val unauthorizedResponse: String = Serializer.instance.toJson(Result(
            ResultStatus.FAILURE,
            null,
            clients.map {
                when (it)
                {
                    is GithubDirectClient -> ErrorInfo("github-token-invalid",
                            "GitHub token not supplied in Authorization header, or GitHub token was invalid")
                    is JWTHeaderClient -> ErrorInfo("bearer-token-invalid",
                            "Bearer token not supplied in Authorization header, or bearer token was invalid")
                    is JWTCookieClient -> ErrorInfo(
                            "cookie-bearer-token-invalid",
                            "Bearer token not supplied in cookie '${JWTCookieClient.cookie}', or bearer token was invalid"
                    )
                    is JWTParameterClient -> ErrorInfo("onetime-token-invalid", "Onetime token not supplied, or onetime token was invalid")
                }
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
