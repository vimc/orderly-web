package org.vaccineimpact.reporting_api.tests.integration_tests.tests

import com.github.fge.jackson.JsonLoader
import org.assertj.core.api.Assertions
import org.junit.Test
import org.vaccineimpact.reporting_api.ContentTypes
import org.vaccineimpact.reporting_api.db.Orderly
import org.vaccineimpact.reporting_api.tests.insertReport
import java.net.URLEncoder

class OnetimeTokenTests : IntegrationTest()
{

    @Test
    fun `gets one time token`()
    {
        insertReport("testname", "testversion")
        val response = requestHelper.get("/onetime_token/?url=test")

        assertJsonContentType(response)
        assertSuccessful(response)
        JSONValidator.validateAgainstSchema(response.text, "Token")

    }


    @Test
    fun `can use one time token to authenticate`()
    {
        val publishedVersion = Orderly().getReportsByName("other")[0]
        val url = "/reports/other/versions/$publishedVersion/artefacts/graph.png/"

        val tokenReponse = requestHelper.get("/onetime_token/?url=" + URLEncoder.encode("/v1$url", "UTF-8"))
        val token = JsonLoader.fromString(tokenReponse.text)["data"].textValue()

        val response = requestHelper.getNoAuth("$url?access_token=$token", ContentTypes.binarydata)

        assertSuccessful(response)
        Assertions.assertThat(response.headers["content-type"]).isEqualTo("image/png")
        Assertions.assertThat(response.headers["content-disposition"]).isEqualTo("attachment; filename=other/$publishedVersion/graph.png")


    }


}