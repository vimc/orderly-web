package org.vaccineimpact.reporting_api.tests.integration_tests.tests

import org.assertj.core.api.Assertions
import org.junit.Test
import org.vaccineimpact.reporting_api.ContentTypes
import org.vaccineimpact.reporting_api.db.Config
import org.vaccineimpact.reporting_api.db.JooqContext
import org.vaccineimpact.reporting_api.db.Orderly
import org.vaccineimpact.reporting_api.db.Tables
import org.vaccineimpact.reporting_api.tests.insertReport
import java.io.File

class ArtefactTests: IntegrationTest()
{
    @Test
    fun `gets dict of artefact names to hashes`(){

        insertReport("testname", "testversion")
        val response = requestHelper.get("/reports/testname/testversion/artefacts")

        assertJsonContentType(response)
        assertSuccessful(response)
        JSONValidator.validateAgainstSchema(response.text, "Dictionary")
    }

    @Test
    fun `gets artefact file`()
    {
        val publishedVersion = Orderly().getReportsByName("other")[0]

        val response = requestHelper.get("/reports/other/$publishedVersion/artefacts/graph.png", ContentTypes.binarydata)

        assertSuccessful(response)
        Assertions.assertThat(response.headers["content-type"]).isEqualTo("application/octet-stream")
        Assertions.assertThat(response.headers["content-disposition"]).isEqualTo("attachment; filename=other/$publishedVersion/graph.png")
    }

    @Test
    fun `gets 404 if artefact doesnt exist in db`()
    {
        insertReport("testname", "testversion")
        val fakeartefact = "hf647rhj"
        val response = requestHelper.get("/reports/testname/testversion/artefacts/$fakeartefact", ContentTypes.binarydata)

        assertJsonContentType(response)
        Assertions.assertThat(response.statusCode).isEqualTo(404)
        JSONValidator.validateError(response.text, "unknown-artefact", "Unknown artefact : '$fakeartefact'")
    }

    @Test
    fun `gets 404 if artefact file doesnt exist`()
    {
        val fakeartefact = "64328fyhdkjs.csv"
        insertReport("testname", "testversion", hashArtefacts = "{\"$fakeartefact\":\"07dffb00305279935544238b39d7b14b\"}")
        val response = requestHelper.get("/reports/testname/testversion/artefacts/$fakeartefact", ContentTypes.binarydata)

        assertJsonContentType(response)
        Assertions.assertThat(response.statusCode).isEqualTo(404)
        JSONValidator.validateError(response.text, "file-not-found", "File with name '$fakeartefact' does not exist")
    }


}
