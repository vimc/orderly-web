package org.vaccineimpact.orderlyweb.security.clients

import org.pac4j.core.context.HttpConstants
import org.pac4j.sparkjava.DefaultHttpActionAdapter
import org.pac4j.sparkjava.SparkWebContext
import org.vaccineimpact.orderlyweb.DirectActionContext
import org.vaccineimpact.orderlyweb.Serializer
import org.vaccineimpact.orderlyweb.addDefaultResponseHeaders
import org.vaccineimpact.orderlyweb.errors.MissingRequiredPermissionError
import org.vaccineimpact.orderlyweb.models.ErrorInfo
import org.vaccineimpact.orderlyweb.models.Result
import org.vaccineimpact.orderlyweb.models.ResultStatus
import org.vaccineimpact.orderlyweb.security.authorization.mismatchedURL
import org.vaccineimpact.orderlyweb.security.authorization.missingPermissions

class TokenActionAdapter(clients: List<OrderlyWebTokenCredentialClient>) : DefaultHttpActionAdapter()
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


    override fun adapt(code: Int, context: SparkWebContext): Any?
    {
        val accept = context.sparkRequest.headers("Accept")

        return when (code)
        {
            HttpConstants.UNAUTHORIZED ->
            {
                if (accept != null && accept.contains("text/html"))
                {
                    val redirectTo = if (context.fullRequestURL.contains("login"))
                    {
                        // the user is trying to log in and is not authenticated,
                        // so redirect to Montagu homepage and request redirect
                        // back once logged in
                        // TODO actually wire this up
                        "/montagu_home?redirectTo=orderly_web"
                    }
                    else
                    {
                        // the user is trying to access a page and is not logged in
                        // redirect to our login flow
                        "/login"
                    }
                    logger.info("User is not authenticated. Redirecting to $redirectTo")
                    context.sparkResponse.redirect(redirectTo)
                }
                else
                {
                    addDefaultResponseHeaders(context.response)
                    spark.Spark.halt(code, unauthorizedResponse)
                }
            }
            HttpConstants.FORBIDDEN ->
            {
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

                if (accept != null && accept.contains("text/html"))
                {
                    logger.info("User is not authorized to view this resource.")
                    logger.warn(authenticationErrors.map { it.message }.joinToString("\n"))
                    context.sparkResponse.redirect("/404")
                }
                else
                {
                    addDefaultResponseHeaders(context.response)
                    spark.Spark.halt(code, forbiddenResponse(authenticationErrors))
                }

            }
            else -> super.adapt(code, context)
        }
    }
}

