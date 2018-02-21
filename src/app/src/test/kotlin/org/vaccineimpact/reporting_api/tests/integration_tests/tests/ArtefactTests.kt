package org.vaccineimpact.reporting_api.tests.integration_tests.tests

import org.assertj.core.api.Assertions
import org.junit.Test
import org.vaccineimpact.reporting_api.ContentTypes
import org.vaccineimpact.reporting_api.db.Orderly
import org.vaccineimpact.reporting_api.security.WebTokenHelper
import org.vaccineimpact.reporting_api.tests.insertReport

class ArtefactTests : IntegrationTest()
{
    @Test
    fun `gets dict of artefact names to hashes`()
    {
        insertReport("testname", "testversion")
        val response = requestHelper.get("/reports/testname/versions/testversion/artefacts/")

        assertJsonContentType(response)
        assertSuccessful(response)
        JSONValidator.validateAgainstSchema(response.text, "Dictionary")
    }

    @Test
    fun `gets artefact file with access token`()
    {
        val publishedVersion = Orderly().getReportsByName("other")[0]

        val url = "/reports/other/versions/$publishedVersion/artefacts/graph.png/"
        val token = requestHelper.generateOnetimeToken(url)
        val response = requestHelper.getNoAuth("$url?access_token=$token", ContentTypes.binarydata)

        assertSuccessful(response)
        Assertions.assertThat(response.headers["content-type"]).isEqualTo("image/png")
        Assertions.assertThat(response.headers["content-disposition"]).isEqualTo("attachment; filename=other/$publishedVersion/graph.png")
    }

    @Test
    fun `gets artefact file with bearer token`()
    {
        val publishedVersion = Orderly().getReportsByName("other")[0]

        val url = "/reports/other/versions/$publishedVersion/artefacts/graph.png/"
        val response = requestHelper.get(url, ContentTypes.binarydata)

        assertSuccessful(response)
        Assertions.assertThat(response.headers["content-type"]).isEqualTo("image/png")
        Assertions.assertThat(response.headers["content-disposition"]).isEqualTo("attachment; filename=other/$publishedVersion/graph.png")
    }

    @Test
    fun `gets 404 if artefact doesnt exist in db`()
    {
        insertReport("testname", "testversion")
        val fakeartefact = "hf647rhj"
        val url = "/reports/testname/versions/testversion/artefacts/$fakeartefact/"
        val token = requestHelper.generateOnetimeToken(url)
        val response = requestHelper.getNoAuth("$url?access_token=$token", ContentTypes.binarydata)

        assertJsonContentType(response)
        Assertions.assertThat(response.statusCode).isEqualTo(404)
        JSONValidator.validateError(response.text, "unknown-artefact", "Unknown artefact : '$fakeartefact'")
    }

    @Test
    fun `gets 401 if missing access token and no auth`()
    {
        insertReport("testname", "testversion")
        val fakeartefact = "hf647rhj"

        val url = "/reports/testname/versions/testversion/artefacts/$fakeartefact"
        val response = requestHelper.getNoAuth("$url/", ContentTypes.binarydata)

        Assertions.assertThat(response.headers["content-type"]).isEqualTo("application/json")
        Assertions.assertThat(response.statusCode).isEqualTo(401)
        JSONValidator.validateMultipleAuthErrors(response.text)
    }

    @Test
    fun `gets 401 if invalid access token`()
    {
        insertReport("testname", "testversion")
        val response = requestHelper.getNoAuth("/reports/testname/versions/testversion/artefacts/fakeartefact/?access_token=42678iwek", ContentTypes.binarydata)

        Assertions.assertThat(response.headers["content-type"]).isEqualTo("application/json")
        Assertions.assertThat(response.statusCode).isEqualTo(401)
        JSONValidator.validateMultipleAuthErrors(response.text)
    }

    @Test
    fun `gets 401 if access token not in db`()
    {
        insertReport("testname", "testversion")
        val url = "/reports/testname/versions/testversion/artefacts/6943yhks/"
        val token = WebTokenHelper.oneTimeTokenHelper.issuer
                .generateOnetimeActionToken(requestHelper.fakeUser, url)
        val response = requestHelper
                .getNoAuth("$url?access_token=$token", ContentTypes.binarydata)

        Assertions.assertThat(response.headers["content-type"]).isEqualTo("application/json")
        Assertions.assertThat(response.statusCode).isEqualTo(401)
        JSONValidator.validateMultipleAuthErrors(response.text)
    }


    @Test
    fun `gets 404 if artefact file doesnt exist`()
    {
        val fakeartefact = "64328fyhdkjs.csv"
        val url = "/reports/testname/versions/testversion/artefacts/$fakeartefact/"
        val token = requestHelper.generateOnetimeToken(url)

        insertReport("testname", "testversion", hashArtefacts = "{\"$fakeartefact\":\"07dffb00305279935544238b39d7b14b\"}")
        val response = requestHelper.getNoAuth("$url?access_token=$token", ContentTypes.binarydata)

        assertJsonContentType(response)
        Assertions.assertThat(response.statusCode).isEqualTo(404)
        JSONValidator.validateError(response.text, "file-not-found", "File with name '$fakeartefact' does not exist")
    }


}
