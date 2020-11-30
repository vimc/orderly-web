package org.vaccineimpact.orderlyweb.tests.integration_tests.tests.api

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.orderlyweb.ContentTypes
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.db.getResource
import org.vaccineimpact.orderlyweb.security.WebTokenHelper
import org.vaccineimpact.orderlyweb.tests.integration_tests.helpers.fakeGlobalReportReviewer
import org.vaccineimpact.orderlyweb.tests.integration_tests.tests.IntegrationTest
import java.io.ByteArrayInputStream
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.zip.ZipInputStream

class BundleTests : IntegrationTest()
{
    @Test
    fun `packs report`()
    {
        val response = apiRequestHelper.post("/bundle/pack/minimal/", emptyMap(), ContentTypes.zip, fakeGlobalReportReviewer())
        assertThat(response.statusCode).isEqualTo(200)
        assertThat(response.headers["content-type"]).isEqualTo("application/zip")
        val contents = getZipEntries(response).map { it.split('/', limit = 2).last() }
        assertThat(contents).contains("meta/manifest.rds", "pack/orderly.yml")
    }

    @Test
    fun `packs report fails without required permission`()
    {
        val response = apiRequestHelper.post("/bundle/pack/minimal/", emptyMap(), ContentTypes.zip)
        assertThat(response.statusCode).isEqualTo(403)
    }

    @Test
    fun `imports report()`()
    {
        val reportName = "minimal"
        val reportVersion = "20201126-153124-9a32811a"
        val orderlyRoot = AppConfig()["orderly.root"]

        // Backup orderly repository state
        val dbPath = Paths.get("${orderlyRoot}orderly.sqlite")
        val dbContent = Files.readAllBytes(dbPath)

        val token = WebTokenHelper.instance.issuer.generateBearerToken(fakeGlobalReportReviewer())
        val body = getResource("$reportVersion.zip").readBytes()
        val request = Request.Builder()
                .url("${apiRequestHelper.baseUrl}/bundle/import/")
                .header("Authorization", "Bearer $token")
                .post(body.toRequestBody("application/zip".toMediaType()))
                .build()
        val response = OkHttpClient().newCall(request).execute()
        assertThat(response.code).isEqualTo(200)

        // Restore orderly repository state
        Files.write(dbPath, dbContent)
        File("${orderlyRoot}archive/$reportName/$reportVersion/").deleteRecursively()
    }

    private fun getZipEntries(response: khttp.responses.Response): MutableList<String>
    {
        val entries = mutableListOf<String>()
        ZipInputStream(ByteArrayInputStream(response.content)).use { zis ->
            while (true)
            {
                val entry = zis.nextEntry ?: break
                entries.add(entry.name)
            }
        }
        return entries
    }
}
