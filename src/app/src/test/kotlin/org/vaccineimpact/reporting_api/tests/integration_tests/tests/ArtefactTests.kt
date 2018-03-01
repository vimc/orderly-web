package org.vaccineimpact.reporting_api.tests.integration_tests.tests

import org.assertj.core.api.Assertions
import org.junit.Test
import org.vaccineimpact.reporting_api.ContentTypes
import org.vaccineimpact.reporting_api.db.Orderly
import org.vaccineimpact.reporting_api.security.InternalUser
import org.vaccineimpact.reporting_api.security.WebTokenHelper
import org.vaccineimpact.reporting_api.tests.insertReport

class ArtefactTests : IntegrationTest()
{
    @Test
    fun `gets dict of artefact names to hashes with report scoped permission`()
    {
        insertReport("testname", "testversion")
        val response = requestHelper.get("/reports/testname/versions/testversion/artefacts/",
                user = fakeReportReader("testname"))


        assertJsonContentType(response)
        assertSuccessful(response)
        JSONValidator.validateAgainstSchema(response.text, "Dictionary")
    }

    @Test
    fun `cant get artefacts dict if report not within report reading scope`()
    {
        insertReport("testname", "testversion")
        val response = requestHelper.get("/reports/testname/versions/testversion/artefacts/",
                user = fakeReportReader("badreportname"))

        assertUnauthorized(response, "testname")
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
    fun `can get artefact file with scoped report reading permission`()
    {
        val publishedVersion = Orderly().getReportsByName("other")[0]

        val url = "/reports/other/versions/$publishedVersion/artefacts/graph.png/"
        val response = requestHelper.get(url, ContentTypes.binarydata,
                user = fakeReportReader("other"))

        assertSuccessful(response)
    }

    @Test
    fun `can't get artefact file if report not within report reading scope`()
    {
        val publishedVersion = Orderly().getReportsByName("other")[0]

        val url = "/reports/other/versions/$publishedVersion/artefacts/graph.png/"
        val response = requestHelper.get(url, ContentTypes.binarydata,
                user = fakeReportReader("badreportname"))

        assertUnauthorized(response, "other")
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
