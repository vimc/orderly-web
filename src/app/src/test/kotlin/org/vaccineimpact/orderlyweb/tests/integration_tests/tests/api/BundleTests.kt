package org.vaccineimpact.orderlyweb.tests.integration_tests.tests.api

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.orderlyweb.ContentTypes
import org.vaccineimpact.orderlyweb.tests.integration_tests.helpers.fakeGlobalReportReviewer
import org.vaccineimpact.orderlyweb.tests.integration_tests.tests.IntegrationTest
import java.io.ByteArrayInputStream
import java.util.zip.ZipInputStream

class BundleTests : IntegrationTest() {
    @Test
    fun `packs report`() {
        val response = apiRequestHelper.post("/bundle/pack/minimal/", emptyMap(), ContentTypes.zip, fakeGlobalReportReviewer())
        assertThat(response.statusCode).isEqualTo(200)
        assertThat(response.headers["content-type"]).isEqualTo("application/zip")
        val contents = getZipEntries(response).map { it.split('/', limit = 2).last() }
        assertThat(contents).contains("meta/manifest.rds", "pack/orderly.yml")
    }

    private fun getZipEntries(response: khttp.responses.Response): MutableList<String> {
        val entries = mutableListOf<String>()
        ZipInputStream(ByteArrayInputStream(response.content)).use { zis ->
            while (true) {
                val entry = zis.nextEntry ?: break
                entries.add(entry.name)
            }
        }
        return entries
    }
}
