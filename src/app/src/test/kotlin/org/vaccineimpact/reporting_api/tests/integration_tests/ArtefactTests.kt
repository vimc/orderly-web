package org.vaccineimpact.reporting_api.tests.integration_tests

import org.assertj.core.api.Assertions
import org.junit.Test
import org.vaccineimpact.reporting_api.ContentTypes
import org.vaccineimpact.reporting_api.db.Config
import org.vaccineimpact.reporting_api.tests.insertReport
import java.io.File

class ArtefactTests //IntegrationTest()
{
//
//    @Test
//    fun `gets dict of artefact names to hashes`(){
//
//        insertReport("testname", "testversion")
//        val response = requestHelper.get("/reports/testname/testversion/artefacts")
//
//        assertSuccessful(response)
//        JSONValidator.validateAgainstSchema(response.text, "Dictionary")
//    }
//
//    @Test
//    fun `gets artefact file`(){
//
//        val demoVersion = File("${Config["orderly.root"]}/archive/other/").list()[0]
//        val response = requestHelper.get("/reports/other/$demoVersion/artefacts/graph.png", ContentTypes.any)
//
//        assertSuccessful(response)
//        Assertions.assertThat(response.headers["content-type"]).isEqualTo("application/octet-stream")
//        Assertions.assertThat(response.headers["content-disposition"]).isEqualTo("attachment; filename=other/$demoVersion/graph.png")
//    }

}
