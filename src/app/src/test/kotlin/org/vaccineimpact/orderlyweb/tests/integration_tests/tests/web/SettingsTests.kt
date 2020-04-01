package org.vaccineimpact.orderlyweb.tests.integration_tests.tests.web

import com.github.fge.jackson.JsonLoader
import org.assertj.core.api.AssertionsForClassTypes
import org.junit.Test
import org.vaccineimpact.orderlyweb.ContentTypes
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.tests.integration_tests.tests.IntegrationTest

class SettingsTests : IntegrationTest()
{
    private val allowGuestUrl = "/settings/auth_allow_guest/"

    @Test
    fun `only user managers can get auth allow guest`()
    {
       assertWebUrlSecured(allowGuestUrl, setOf(ReifiedPermission("users.manage", Scope.Global())),
                contentType = ContentTypes.json)
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
}