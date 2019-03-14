package org.vaccineimpact.orderlyweb.security

import org.pac4j.core.config.Config
import org.pac4j.core.context.HttpConstants.HTTP_METHOD
import org.pac4j.core.matching.HttpMethodMatcher
import spark.route.HttpMethod

// For every HttpMethod, make sure we have a matcher with a well-known name
// in the format "method:get" that only allows that method.
fun Config.addMethodMatchers()
{
    for (pac4jMethod in HTTP_METHOD.values())
    {
        val sparkMethod = HttpMethod.valueOf(pac4jMethod.name.toLowerCase())
        addMatcher("method:$sparkMethod", HttpMethodMatcher(pac4jMethod))
    }
}