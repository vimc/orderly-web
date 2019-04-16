package org.vaccineimpact.orderlyweb.security

import org.pac4j.core.context.HttpConstants
import org.pac4j.sparkjava.DefaultHttpActionAdapter
import org.pac4j.sparkjava.SparkWebContext
import org.vaccineimpact.orderlyweb.errors.FailedLoginError
import org.vaccineimpact.orderlyweb.errors.RouteNotFound

class WebActionAdaptor : DefaultHttpActionAdapter()
{
    override fun adapt(code: Int, context: SparkWebContext): Any? = when (code)
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
        else -> super.adapt(code, context)
    }
}
