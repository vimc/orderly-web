package org.vaccineimpact.orderlyweb.tests.integration_tests.tests.web

import com.github.fge.jackson.JsonLoader
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.Test
import org.vaccineimpact.orderlyweb.ContentTypes
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.tests.integration_tests.tests.IntegrationTest

class PermissionTests : IntegrationTest()
{
    @Test
    fun `only user managers can see permission list`()
    {
        val url = "/typeahead/permissions/"
        assertWebUrlSecured(url, setOf(ReifiedPermission("users.manage", Scope.Global())),
                contentType = ContentTypes.json)
    }

    @Test
    fun `can get permission list`()
    {
        val url = "/typeahead/permissions/"
        val response = webRequestHelper.loginWithMontaguAndMakeRequest(url,
                setOf(ReifiedPermission("users.manage", Scope.Global())),
                ContentTypes.json)

        val json = JsonLoader.fromString(response.text)
        assertThat(json["data"].toString())
                .isEqualTo("[\"documents.manage\",\"documents.read\",\"pinned-reports.manage\",\"reports.read\"," +
                        "\"reports.review\",\"reports.run\",\"tags.manage\",\"users.manage\"]")
    }
}
