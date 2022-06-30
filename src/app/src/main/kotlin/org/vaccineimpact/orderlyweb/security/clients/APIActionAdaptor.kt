package org.vaccineimpact.orderlyweb.security.clients

import com.github.scribejava.core.extractors.TokenExtractor
import org.pac4j.core.context.HttpConstants
import org.pac4j.core.context.WebContext
import org.pac4j.core.exception.CredentialsException
import org.pac4j.core.exception.http.HttpAction
import org.pac4j.core.http.adapter.HttpActionAdapter
import org.pac4j.jee.context.session.JEESessionStore
import org.pac4j.sparkjava.SparkHttpActionAdapter
import org.pac4j.sparkjava.SparkWebContext
import org.vaccineimpact.orderlyweb.DirectActionContext
import org.vaccineimpact.orderlyweb.Serializer
import org.vaccineimpact.orderlyweb.addDefaultResponseHeaders
import org.vaccineimpact.orderlyweb.errors.ExpiredToken
import org.vaccineimpact.orderlyweb.errors.MissingRequiredPermissionError
import org.vaccineimpact.orderlyweb.models.ErrorInfo
import org.vaccineimpact.orderlyweb.models.Result
import org.vaccineimpact.orderlyweb.models.ResultStatus
import org.vaccineimpact.orderlyweb.security.authorization.mismatchedURL
import org.vaccineimpact.orderlyweb.security.authorization.missingPermissions

class APIActionAdaptor(private val clients: List<OrderlyWebTokenCredentialClient>) : SparkHttpActionAdapter()
{
    private fun unauthorizedResponse(e: Exception?): String
    {
        val problems = if (e is ExpiredToken)
        {
            e.problems
        } else clients.map {
            it.errorInfo
        }

        return Serializer.instance.toJson(Result(
                ResultStatus.FAILURE,
                null,
                problems))
    }

    private fun forbiddenResponse(authenticationErrors: List<ErrorInfo>): String = Serializer.instance.toJson(Result(
            ResultStatus.FAILURE,
            null,
            authenticationErrors
    ))


    override fun adapt(action: HttpAction, context: WebContext): Any? = when (action.code)
    {
        HttpConstants.UNAUTHORIZED ->
        {
            val e = JEESessionStore.INSTANCE.get(context, "token_exception")
            addDefaultResponseHeaders((context as SparkWebContext).sparkResponse.raw())
            spark.Spark.halt(action.code, unauthorizedResponse((if (e.isPresent) e.get() else null) as Exception?))
        }
        HttpConstants.FORBIDDEN ->
        {
            addDefaultResponseHeaders((context as SparkWebContext).sparkResponse.raw())

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

            spark.Spark.halt(action.code, forbiddenResponse(authenticationErrors))
        }
        else -> super.adapt(action, context)
    }
}
