package org.vaccineimpact.orderlyweb.blackboxTests

import khttp.structures.authorization.Authorization
import org.assertj.core.api.Assertions
import org.junit.Test

class AuthenticationTests : CustomConfigTests()
{
    @Test
    fun `BadConfiguration errors are returned to the client`()
    {
        val fakeConfig = "app.github_org=hdyeiksn"
        runWithConfig(fakeConfig)
        Thread.sleep(1000)
        val token = "db5920039c7d88fd976cbdab1da8e531c1148fcf".reversed()
        val result = khttp.post("http://localhost:8081/api/v1/login", auth = GithubTokenHeader(token))
        Assertions.assertThat(result.statusCode).isEqualTo(500)
        Assertions.assertThat(result.text).contains("GitHub org hdyeiksn does not exist")
    }

    data class GithubTokenHeader(val token: String, val prefix: String = "token") : Authorization
    {
        override val header: Pair<String, String>
            get()
            {
                return "Authorization" to "$prefix $token"
            }
    }
}