package org.vaccineimpact.orderlyweb.security

import org.pac4j.core.context.HttpConstants
import org.pac4j.core.context.WebContext
import org.pac4j.core.exception.http.HttpAction
import org.pac4j.sparkjava.SparkHttpActionAdapter
import org.vaccineimpact.orderlyweb.errors.FailedLoginError
import org.vaccineimpact.orderlyweb.errors.RouteNotFound

class WebActionAdaptor : SparkHttpActionAdapter()
{
    override fun adapt(action: HttpAction, context: WebContext): Any? = when (action.code)
    {
        // these errors will be handled by the global error handler
        HttpConstants.UNAUTHORIZED ->
        {
            throw FailedLoginError()
        }
        HttpConstants.FORBIDDEN ->
        {
            throw RouteNotFound()
        }
        else -> super.adapt(action, context)
    }
}
