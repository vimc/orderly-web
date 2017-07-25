package org.vaccineimpact.reporting_api

import org.slf4j.LoggerFactory
import org.vaccineimpact.reporting_api.db.TokenStore
import org.vaccineimpact.reporting_api.errors.InvalidOneTimeLinkToken
import org.vaccineimpact.reporting_api.security.TokenIssuer
import org.vaccineimpact.reporting_api.security.TokenVerifier
import org.vaccineimpact.reporting_api.security.WebTokenHelper
import spark.Filter
import spark.Request
import spark.Response
import spark.Spark
import spark.route.HttpMethod

data class OnetimeTokenEndpoint(
        override val urlFragment: String,
        override val controllerName: String,
        override val actionName: String,
        override val contentType: String = ContentTypes.binarydata,
        override val method: HttpMethod = HttpMethod.get
) : EndpointDefinition
{
    init
    {
        if (!urlFragment.endsWith("/"))
        {
            throw Exception("All endpoint definitions must end with a forward slash: $urlFragment")
        }
    }

    override fun additionalSetup(url: String)
    {
        Spark.before(url, OneTimeTokenFilter(WebTokenHelper.oneTimeTokenHelper.verifier))
    }

}

class OneTimeTokenFilter(val tokenVerifier: TokenVerifier, val tokenStore: TokenStore = TokenStore()) : Filter
{

    override fun handle(request: Request, response: Response)
    {
        val token = request.queryParams("access_token")
                ?: throw InvalidOneTimeLinkToken("verification", "Access token is missing")

        // TODO at some point may need to pass the roles/permissions through
        val claims = verifyToken(token)
    }

    private val logger = LoggerFactory.getLogger(OneTimeTokenFilter::class.java)

    private fun verifyToken(token: String): Map<String, Any>
    {
        // By checking the database first, we ensure the token is
        // removed from the database, even if it fails some later check
        if (!tokenStore.validateOneTimeToken(token))
        {
            throw InvalidOneTimeLinkToken("used", "Token has already been used (or never existed)")
        }

        val claims = try
        {
            tokenVerifier.verify(token)
        }
        catch (e: Exception)
        {
            logger.warn("An error occurred validating the onetime link token: $e")
            throw InvalidOneTimeLinkToken("verification", "Unable to verify token; it may be badly formatted or signed with the wrong key")
        }
        if (claims["sub"] != TokenIssuer.oneTimeActionSubject)
        {
            throw InvalidOneTimeLinkToken("subject", "Expected 'sub' claim to be ${TokenIssuer.oneTimeActionSubject}")
        }
        return claims
    }
}