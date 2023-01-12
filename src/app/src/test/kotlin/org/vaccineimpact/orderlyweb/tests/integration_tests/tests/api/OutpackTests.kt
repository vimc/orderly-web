package org.vaccineimpact.orderlyweb.tests.integration_tests.tests.api

import com.github.salomonbrys.kotson.toJsonArray
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.vaccineimpact.orderlyweb.ContentTypes
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.tests.integration_tests.helpers.fakeGlobalReportReader
import org.vaccineimpact.orderlyweb.tests.integration_tests.tests.IntegrationTest
import java.io.File

class OutpackTests : IntegrationTest()
{
    @Test
    fun `can get outpack index`()
    {
        val response = apiRequestHelper.get(
                "/outpack/",
                userEmail = fakeGlobalReportReader()
        )

        assertJsonContentType(response)
        assertSuccessful(response)
        Assertions.assertThat(JSONValidator.getData(response.text).has("schema_version")).isTrue
    }

    @Test
    fun `can get arbitrary outpack route`()
    {
        val response = apiRequestHelper.get(
                "/outpack/metadata/list",
                userEmail = fakeGlobalReportReader()
        )

        assertJsonContentType(response)
        assertSuccessful(response)
        Assertions.assertThat(JSONValidator.getData(response.text).isArray).isTrue
    }

    @Test
    fun `can get outpack file`()
    {
        val metadataList = apiRequestHelper.get(
                "/outpack/metadata/list",
                userEmail = fakeGlobalReportReader()
        )

        val packet = JSONValidator.getData(metadataList.text).get(0).get("packet").asText()

        val metadataPacket = apiRequestHelper.get(
                "/outpack/metadata/$packet/json/",
                userEmail = fakeGlobalReportReader()
        )

        val hash = JSONValidator.getData(metadataPacket.text).get("files").get(0).get("hash").asText()

        val response = apiRequestHelper.get(
                "/outpack/file/$hash",
                contentType = ContentTypes.binarydata,
                userEmail = fakeGlobalReportReader()
        )

        assertSuccessful(response)
        Assertions.assertThat(response.headers["content-type"]).isEqualTo("application/octet-stream")
        Assertions.assertThat(response.headers["content-disposition"]).isEqualTo("attachment; filename=\"$hash\"")
        Assertions.assertThat(response.headers["content-length"]!!.toInt()).isGreaterThan(0)

        val hashContent = hash.split(":")[1]
        val hashPath = "${hashContent.substring(0, 2)}/${hashContent.substring(2)}"
        val expectedFile = File("${AppConfig()["outpack.test.root"]}/.outpack/files/sha256/$hashPath")
        Assertions.assertThat(response.text).isEqualTo(expectedFile.readText())
    }

    @Test
    fun `can get json errors for outpack file`()
    {
        val response = apiRequestHelper.get(
                "/outpack/file/12345",
                userEmail = fakeGlobalReportReader()
        )

        assertJsonContentType(response)
        Assertions.assertThat(response.statusCode).isEqualTo(404)
    }
}
