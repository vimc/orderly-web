package org.vaccineimpact.orderlyweb.security

import org.pac4j.core.context.HttpConstants
import org.pac4j.core.context.WebContext
import org.pac4j.core.context.session.SessionStore
import org.pac4j.core.matching.matcher.Matcher

/** Security will not be applied if the request's HTTP method is one
 * of the methods that this matcher is set to skip.
 */
open class SkipMethodsMatcher(methodsToSkip: List<HttpConstants.HTTP_METHOD>) : Matcher
{
    val methodsToSkip = methodsToSkip.map { it.name.lowercase() }

    override fun matches(context: WebContext, sessionStore: SessionStore): Boolean
    {
        val requestMethod = context.requestMethod.lowercase()
        return !methodsToSkip.contains(requestMethod)
    }
}

object SkipOptionsMatcher : SkipMethodsMatcher(listOf(HttpConstants.HTTP_METHOD.OPTIONS))
{
    const val name = "SkipOptions"
}
