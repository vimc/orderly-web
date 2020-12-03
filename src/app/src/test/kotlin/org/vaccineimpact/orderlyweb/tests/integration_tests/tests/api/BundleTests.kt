package org.vaccineimpact.orderlyweb.tests.integration_tests.tests.api

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.assertj.core.api.Assertions.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.vaccineimpact.orderlyweb.ContentTypes
import org.vaccineimpact.orderlyweb.db.getResource
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.security.WebTokenHelper
import org.vaccineimpact.orderlyweb.tests.integration_tests.helpers.fakeGlobalReportReviewer
import org.vaccineimpact.orderlyweb.tests.integration_tests.tests.IntegrationTest
import spark.route.HttpMethod
import java.io.ByteArrayInputStream
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.zip.ZipInputStream
import kotlin.properties.Delegates

class BundlePackTests : IntegrationTest()
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
        assertAPIUrlSecured("/bundle/pack/minimal/",
                setOf(ReifiedPermission("reports.run", Scope.Global())),
                ContentTypes.zip, HttpMethod.post)
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

class BundleImportTests : IntegrationTest()
{
    private val reportName = "minimal"

    // This is a simply a unique identifier - it does not depend on the contents of the orderly store
    private val reportVersion = "20201126-153124-9a32811a"

    // Note that this is orderly.server's MONTAGU_ORDERLY_PATH, not OrderlyWeb's orderly_root
    private val orderlyServerRoot = "git/"

    private var dbPath: Path by Delegates.notNull()
    private var dbContent: ByteArray by Delegates.notNull()

    @Before
    fun `backup orderly state`()
    {
        dbPath = Paths.get("${orderlyServerRoot}orderly.sqlite")
        dbContent = Files.readAllBytes(dbPath)
    }

    @Test
    fun `imports report()`()
    {
        val token = WebTokenHelper.instance.issuer.generateBearerToken(fakeGlobalReportReviewer())
        val body = getResource("$reportVersion.zip").readBytes()
        val request = Request.Builder()
                .url("${apiRequestHelper.baseUrl}/bundle/import/")
                .header("Authorization", "Bearer $token")
                .post(body.toRequestBody("application/zip".toMediaType()))
                .build()
        val response = OkHttpClient().newCall(request).execute()
        assertThat(response.code).isEqualTo(200)
    }

    @After
    fun `restore orderly state`()
    {
        Files.write(dbPath, dbContent)
        File("${orderlyServerRoot}archive/$reportName/$reportVersion/").deleteRecursively()
    }
}
