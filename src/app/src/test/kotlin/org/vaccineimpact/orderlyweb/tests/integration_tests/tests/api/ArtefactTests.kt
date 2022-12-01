package org.vaccineimpact.orderlyweb.tests.integration_tests.tests.api

import org.assertj.core.api.Assertions
import org.junit.Test
import org.vaccineimpact.orderlyweb.ContentTypes
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.Orderly
import org.vaccineimpact.orderlyweb.db.repositories.OrderlyReportRepository
import org.vaccineimpact.orderlyweb.db.repositories.ReportRepository
import org.vaccineimpact.orderlyweb.models.FileInfo
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.test_helpers.insertReport
import org.vaccineimpact.orderlyweb.tests.insertArtefact
import org.vaccineimpact.orderlyweb.tests.integration_tests.helpers.fakeGlobalReportReviewer
import org.vaccineimpact.orderlyweb.tests.integration_tests.helpers.fakeReportReader
import org.vaccineimpact.orderlyweb.tests.integration_tests.tests.IntegrationTest
import java.io.File

class ArtefactTests : IntegrationTest()
{
    @Test
    fun `report readers can get dict of artefact names to hashes`()
    {
        insertReport("testname", "testversion")
        val response = apiRequestHelper.get("/reports/testname/versions/testversion/artefacts/",
                userEmail = fakeReportReader("testname"))


        assertJsonContentType(response)
        assertSuccessful(response)
        JSONValidator.validateAgainstSchema(response.text, "Dictionary")
    }

    @Test
    fun `only report readers can get artefacts dict`()
    {
        insertReport("testname", "testversion")
        assertAPIUrlSecured("/reports/testname/versions/testversion/artefacts/",
                setOf(ReifiedPermission("reports.read", Scope.Specific("report", "testname"))),
                ContentTypes.json)
    }

    @Test
    fun `gets artefact file with bearer token`()
    {
        val publishedVersion = OrderlyReportRepository(false, true, listOf()).getReportsByName("other")[0]

        val url = "/reports/other/versions/$publishedVersion/artefacts/graph.png/"
        val response = apiRequestHelper.get(url, ContentTypes.binarydata)

        assertSuccessful(response)
        Assertions.assertThat(response.headers["content-type"]).isEqualTo("image/png")
        Assertions.assertThat(response.headers["content-disposition"]).isEqualTo("attachment; filename=\"other/$publishedVersion/graph.png\"")
    }

    @Test
    fun `gets artefact file with bearer token without trailing slash`()
    {
        val publishedVersion = OrderlyReportRepository(false, true, listOf()).getReportsByName("other")[0]

        val url = "/reports/other/versions/$publishedVersion/artefacts/graph.png"
        val response = apiRequestHelper.get(url, ContentTypes.binarydata)

        assertSuccessful(response)
        Assertions.assertThat(response.headers["content-type"]).isEqualTo("image/png")
        Assertions.assertThat(response.headers["content-disposition"]).isEqualTo("attachment; filename=\"other/$publishedVersion/graph.png\"")
    }

    @Test
    fun `gets artefact file with space in name`()
    {
        val version = File("${AppConfig()["orderly.root"]}/archive/spaces/").list()[0]

        val url = "/reports/spaces/versions/$version/artefacts/a+graph+with+spaces.png"
        val response = apiRequestHelper.get(url, ContentTypes.binarydata, userEmail = fakeGlobalReportReviewer())

        assertSuccessful(response)
        Assertions.assertThat(response.headers["content-type"]).isEqualTo("image/png")
        Assertions.assertThat(response.headers["content-disposition"]).isEqualTo("attachment; filename=\"spaces/$version/a graph with spaces.png\"")
    }

    @Test
    fun `gets artefact file with quotes in name`()
    {
        val version = File("${AppConfig()["orderly.root"]}/archive/spaces/").list()[0]

        val url = "/reports/spaces/versions/$version/artefacts/mygraph%27s.png"
        val response = apiRequestHelper.get(url, ContentTypes.binarydata, userEmail = fakeGlobalReportReviewer())

        assertSuccessful(response)
        Assertions.assertThat(response.headers["content-type"]).isEqualTo("image/png")
        Assertions.assertThat(response.headers["content-disposition"]).isEqualTo("attachment; filename=\"spaces/$version/mygraph's.png\"")
    }

    @Test
    fun `only report readers can download artefact file`()
    {
        val publishedVersion = OrderlyReportRepository(false, true, listOf()).getReportsByName("other")[0]

        val url = "/reports/other/versions/$publishedVersion/artefacts/graph.png/"

        assertAPIUrlSecured(url,
                setOf(ReifiedPermission("reports.read", Scope.Specific("report", "other"))),
                ContentTypes.binarydata)
    }

    @Test
    fun `gets 404 if artefact doesnt exist in db`()
    {
        insertReport("testname", "testversion")
        val fakeartefact = "hf647rhj"
        val url = "/reports/testname/versions/testversion/artefacts/$fakeartefact/"
        val response = apiRequestHelper.get(url, ContentTypes.binarydata)

        assertJsonContentType(response)
        Assertions.assertThat(response.statusCode).isEqualTo(404)
        JSONValidator.validateError(response.text, "unknown-artefact", "Unknown artefact : '$fakeartefact'")
    }

    @Test
    fun `gets 404 if artefact file doesnt exist`()
    {
        val fakeartefact = "64328fyhdkjs.csv"
        val url = "/reports/testname/versions/testversion/artefacts/$fakeartefact/"

        insertReport("testname", "testversion")
        insertArtefact("testversion", files = listOf(FileInfo(fakeartefact, 1)))
        val response = apiRequestHelper.get(url, ContentTypes.binarydata)

        assertJsonContentType(response)
        Assertions.assertThat(response.statusCode).isEqualTo(404)
        JSONValidator.validateError(response.text, "file-not-found", "File with name '$fakeartefact' does not exist")
    }


}
