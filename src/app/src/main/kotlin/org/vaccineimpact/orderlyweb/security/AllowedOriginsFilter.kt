package org.vaccineimpact.orderlyweb.security

import spark.Filter
import spark.Request
import spark.Response

class AllowedOriginsFilter(private val allowLocalhost: Boolean) : Filter
{
    private val allowedRegex =
            Regex("^https://((support.montagu.dide.ic.ac.uk:\\d*/.*)|(montagu\\.vaccineimpact\\.org/.*))")

    private val localRegex = Regex("^https?://localhost.*")

    override fun handle(request: Request, response: Response)
    {
        val origin = request.headers("Origin")
        if (origin.isNullOrEmpty())
        {
            return
        }

        if (allowedRegex.matches(origin) || (allowLocalhost && localRegex.matches(origin)))
        {
            response.raw().addHeader("Access-Control-Allow-Origin", origin)
        }
    }
}
