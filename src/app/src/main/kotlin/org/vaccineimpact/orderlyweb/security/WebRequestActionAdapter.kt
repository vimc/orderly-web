package org.vaccineimpact.orderlyweb.security

import org.pac4j.core.context.HttpConstants
import org.pac4j.sparkjava.DefaultHttpActionAdapter
import org.pac4j.sparkjava.SparkWebContext

class WebRequestActionAdapter() : DefaultHttpActionAdapter()
{
    override fun adapt(code: Int, context: SparkWebContext): Any? = when (code)
    {
        HttpConstants.UNAUTHORIZED ->
        {
            context.sparkResponse.redirect("/login")
        }
        HttpConstants.FORBIDDEN ->
        {
            context.sparkResponse.redirect("/")
        }
        else -> super.adapt(code, context)
    }
}
