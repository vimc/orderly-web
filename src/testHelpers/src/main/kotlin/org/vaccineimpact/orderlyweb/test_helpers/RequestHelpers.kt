package org.vaccineimpact.orderlyweb.test_helpers
import khttp.structures.authorization.Authorization

data class GithubTokenHeader(val token: String, val prefix: String = "token") : Authorization
{
    override val header: Pair<String, String>
        get()
        {
            return "Authorization" to "$prefix $token"
        }
}