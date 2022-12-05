package org.vaccineimpact.orderlyweb.tests.integration_tests.tests.web

import com.github.fge.jackson.JsonLoader
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.AssertionsForClassTypes
import org.junit.jupiter.api.Test
import org.vaccineimpact.orderlyweb.ContentTypes
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.tests.integration_tests.tests.IntegrationTest
import spark.route.HttpMethod

class SettingsTests : IntegrationTest()
{
    private val allowGuestUrl = "/settings/auth-allow-guest/"
    @Test
    fun `only user managers can get auth allow guest`()
    {
       assertWebUrlSecured(allowGuestUrl, setOf(ReifiedPermission("users.manage", Scope.Global())),
                contentType = ContentTypes.json)
    }

    @Test
    fun `non-user managers cannot set auth allow guest`()
    {
        val response = webRequestHelper.loginWithMontaguAndMakeRequest(allowGuestUrl,
                setOf(),
                ContentTypes.json,
                method = HttpMethod.post,
                data = "true")

        assertThat(response.statusCode).isEqualTo(404)
    }

    @Test
    fun `can get auth allow guest`()
    {
        val response = webRequestHelper.loginWithMontaguAndMakeRequest(allowGuestUrl,
                setOf(ReifiedPermission("users.manage", Scope.Global())),
                ContentTypes.json)

        val json = JsonLoader.fromString(response.text)
        AssertionsForClassTypes.assertThat(json["data"].toString())
                .isEqualTo("false")
    }

    @Test
    fun `can get expected set auth allow guest response`()
    {
        val response = webRequestHelper.loginWithMontaguAndMakeRequest(allowGuestUrl,
                setOf(ReifiedPermission("users.manage", Scope.Global())),
                ContentTypes.json,
                method = HttpMethod.post,
                data = "true")

        assertThat(response.statusCode).isEqualTo(400)
        val json = JsonLoader.fromString(response.text)
        AssertionsForClassTypes.assertThat(json["errors"][0]["code"].asText())
                .isEqualTo("invalid-operation-error")

    }
}