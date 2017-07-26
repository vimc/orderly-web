package org.vaccineimpact.reporting_api.security

import org.pac4j.core.context.HttpConstants
import org.pac4j.core.context.WebContext
import org.pac4j.core.matching.Matcher

/** Security will not be applied if the request's HTTP method is one
 * of the methods that this matcher is set to skip.
 */
open class SkipMethodsMatcher(methodsToSkip: List<HttpConstants.HTTP_METHOD>) : Matcher {
    val methodsToSkip = methodsToSkip.map { it.name.toLowerCase() }

    override fun matches(context: WebContext): Boolean {
        val requestMethod = context.requestMethod.toLowerCase()
        return !methodsToSkip.contains(requestMethod)
    }
}

object SkipOptionsMatcher : SkipMethodsMatcher(listOf(HttpConstants.HTTP_METHOD.OPTIONS)) {
    val name = "SkipOptions"
}