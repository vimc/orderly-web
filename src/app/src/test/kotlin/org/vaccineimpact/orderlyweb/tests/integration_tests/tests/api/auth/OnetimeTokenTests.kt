package org.vaccineimpact.orderlyweb.tests.integration_tests.tests.api.auth

import com.github.fge.jackson.JsonLoader
import org.assertj.core.api.Assertions
import org.junit.Test
import org.vaccineimpact.orderlyweb.ContentTypes
import org.vaccineimpact.orderlyweb.db.repositories.OrderlyReportRepository
import org.vaccineimpact.orderlyweb.test_helpers.insertReport
import org.vaccineimpact.orderlyweb.tests.integration_tests.tests.IntegrationTest
import java.net.URLEncoder

class OnetimeTokenTests : IntegrationTest()
{

    @Test
    fun `gets one time token`()
    {
        insertReport("testname", "testversion")
        val response = apiRequestHelper.get("/onetime_token/?url=test")

        assertJsonContentType(response)
        assertSuccessful(response)
        JSONValidator.validateAgainstSchema(response.text, "Token")
    }

    @Test
    fun `can use one time token to authenticate`()
    {
        val publishedVersion = OrderlyReportRepository(false, true, listOf()).getReportsByName("other")[0]
        val url = "/reports/other/versions/$publishedVersion/artefacts/graph.png/"

        val tokenReponse = apiRequestHelper.get("/onetime_token/?url=" + URLEncoder.encode("/api/v1$url", "UTF-8"))
        val token = JsonLoader.fromString(tokenReponse.text)["data"].textValue()

        val response = apiRequestHelper.getNoAuth("$url?access_token=$token", ContentTypes.binarydata)

        assertSuccessful(response)
        Assertions.assertThat(response.headers["content-type"]).isEqualTo("image/png")
        Assertions.assertThat(response.headers["content-disposition"]).isEqualTo("attachment; filename=\"other/$publishedVersion/graph.png\"")
    }

    @Test
    fun `can use one time token to authenticate endpoint with query string`()
    {
        val publishedVersion = OrderlyReportRepository(false, true, listOf()).getReportsByName("other")[0]
        val url = "/reports/other/versions/$publishedVersion/artefacts/graph.png/?query=whatever"

        val tokenReponse = apiRequestHelper.get("/onetime_token/?url=" + URLEncoder.encode("/api/v1$url", "UTF-8"))
        val token = JsonLoader.fromString(tokenReponse.text)["data"].textValue()

        val response = apiRequestHelper.getNoAuth("$url&access_token=$token", ContentTypes.binarydata)

        assertSuccessful(response)
        Assertions.assertThat(response.headers["content-type"]).isEqualTo("image/png")
        Assertions.assertThat(response.headers["content-disposition"]).isEqualTo("attachment; filename=\"other/$publishedVersion/graph.png\"")
    }
}
