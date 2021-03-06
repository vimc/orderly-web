package org.vaccineimpact.orderlyweb.test_helpers

import org.vaccineimpact.orderlyweb.test_helpers.http.Authorization

data class TestTokenHeader(val token: String, val prefix: String = "token") : Authorization
{
    override val header: Pair<String, String>
        get()
        {
            return "Authorization" to "$prefix $token"
        }
}