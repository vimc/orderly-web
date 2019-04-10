package org.vaccineimpact.orderlyweb.customConfigTests

import org.assertj.core.api.Assertions
import org.junit.Test

class MontaguAuthenticationTests : CustomConfigTests()
{
    @Test
    fun `user is redirected to Montagu if not logged in`()
    {
        startApp("auth.provider=montagu")

        val response = khttp.get(RequestHelper()
                .webBaseUrl, allowRedirects = false)
        Assertions.assertThat(response.statusCode).isEqualTo(302)
    }

    @Test
    fun `user can login with Montagu cookie`()
    {
        startApp("auth.provider=montagu")

        val loginResponse = RequestHelper()
                .getWithMontaguCookie("/login")

        // the session cookie should now have been set
        // so pull this out of the response and send it back
        val cookie = loginResponse.headers["Set-Cookie"]

        val response = khttp.get(RequestHelper().webBaseUrl,
                headers = mapOf("Cookie" to cookie!!))

        Assertions.assertThat(response.statusCode).isEqualTo(200)
    }

    @Test
    fun `user can logout`()
    {
        startApp("auth.provider=montagu")

        //login
        val loginResponse = RequestHelper().getWithMontaguCookie("/login")

        //logout
        val cookie = loginResponse.headers["Set-Cookie"]
        khttp.get("${RequestHelper().webBaseUrl}/logout",
                headers = mapOf("Cookie" to cookie!!),
                allowRedirects = false)

        //after logout, user should be redirected when attempt to access base url
        val response = khttp.get(RequestHelper().webBaseUrl,
                headers = mapOf("Cookie" to cookie),
                allowRedirects = false)
        Assertions.assertThat(response.statusCode).isEqualTo(302)
    }
}