package org.vaccineimpact.orderlyweb.tests.integration_tests.tests.web

import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.toJsonArray
import org.assertj.core.api.Assertions
import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.vaccineimpact.orderlyweb.ContentTypes
import org.vaccineimpact.orderlyweb.db.JooqContext
import org.vaccineimpact.orderlyweb.db.Tables
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.test_helpers.insertDocument
import org.vaccineimpact.orderlyweb.tests.integration_tests.tests.IntegrationTest
import spark.route.HttpMethod
import java.io.File

class DocumentTests : IntegrationTest()
{
    private val readDocuments = setOf(ReifiedPermission("documents.read", Scope.Global()))
    private val manageDocuments = setOf(ReifiedPermission("documents.manage", Scope.Global()))

    @After
    fun cleanup()
    {
        File("documents").deleteRecursively()
    }

    @Before
    fun setup() {
        File("documents/some/path").mkdirs()
        File("documents/some/path/file.csv").createNewFile()
    }

    @Test
    fun `only document readers can download documents`()
    {
        val url = "/project-docs/some/path/file.csv"
        assertWebUrlSecured(url, readDocuments, contentType = ContentTypes.binarydata)
    }

    @Test
    fun `documents can be downloaded`()
    {
        File("documents/some/path/file.csv").writeText("TEST")
        val sessionCookie = webRequestHelper.webLoginWithMontagu(readDocuments)
        val response = webRequestHelper.requestWithSessionCookie("/project-docs/some/path/file.csv", sessionCookie,
                ContentTypes.binarydata)
        assertSuccessful(response)
        Assertions.assertThat(response.headers["content-type"]).isEqualTo("text/csv")
        Assertions.assertThat(response.headers["content-disposition"]).isEqualTo("attachment; filename=\"some/path/file.csv\"")
        Assertions.assertThat(response.text).isEqualTo("TEST")
    }

    @Test
    fun `only document readers can visit document index page`()
    {
        val url = "/project-docs/"
        assertWebUrlSecured(url, readDocuments)
    }

    @Test
    fun `only document managers can refresh documents`()
    {
        val url = "/documents/refresh/"
        assertWebUrlSecured(url, manageDocuments,
                contentType = ContentTypes.json,
                method = HttpMethod.post,
                postData = mapOf("url" to "https://github.com/vimc/orderly-web/raw/mrc-1458/testdata/test.zip"))
    }

    @Test
    fun `can refresh documents`()
    {
        val sessionCookie = webRequestHelper.webLoginWithMontagu(manageDocuments)
        val response = webRequestHelper.requestWithSessionCookie("/documents/refresh/",
                sessionCookie,
                method = HttpMethod.post,
                contentType = ContentTypes.json,
                postData = mapOf("url" to "https://github.com/vimc/orderly-web/raw/master/testdata/test.zip"))

        Assertions.assertThat(response.statusCode).isEqualTo(200)

        JooqContext().use {
            val result = it.dsl.selectFrom(Tables.ORDERLYWEB_DOCUMENT)
                    .orderBy(Tables.ORDERLYWEB_DOCUMENT.PATH)
                    .fetch()

            Assertions.assertThat(result.count()).isEqualTo(7)

            Assertions.assertThat(result[0][Tables.ORDERLYWEB_DOCUMENT.PATH]).isEqualTo("/test")
            Assertions.assertThat(result[0][Tables.ORDERLYWEB_DOCUMENT.NAME]).isEqualTo("test")
            Assertions.assertThat(result[0][Tables.ORDERLYWEB_DOCUMENT.IS_FILE]).isEqualTo(0)
            Assertions.assertThat(result[0][Tables.ORDERLYWEB_DOCUMENT.PARENT]).isEqualTo(null)
            Assertions.assertThat(result[0][Tables.ORDERLYWEB_DOCUMENT.SHOW]).isEqualTo(1)

            Assertions.assertThat(result[1][Tables.ORDERLYWEB_DOCUMENT.PATH]).isEqualTo("/test/testdata")
            Assertions.assertThat(result[1][Tables.ORDERLYWEB_DOCUMENT.NAME]).isEqualTo("testdata")
            Assertions.assertThat(result[1][Tables.ORDERLYWEB_DOCUMENT.IS_FILE]).isEqualTo(0)
            Assertions.assertThat(result[1][Tables.ORDERLYWEB_DOCUMENT.PARENT]).isEqualTo("/test")
            Assertions.assertThat(result[1][Tables.ORDERLYWEB_DOCUMENT.SHOW]).isEqualTo(1)

            Assertions.assertThat(result[2][Tables.ORDERLYWEB_DOCUMENT.PATH]).isEqualTo("/test/testdata/subdir")
            Assertions.assertThat(result[2][Tables.ORDERLYWEB_DOCUMENT.NAME]).isEqualTo("subdir")
            Assertions.assertThat(result[2][Tables.ORDERLYWEB_DOCUMENT.IS_FILE]).isEqualTo(0)
            Assertions.assertThat(result[2][Tables.ORDERLYWEB_DOCUMENT.PARENT]).isEqualTo("/test/testdata")
            Assertions.assertThat(result[2][Tables.ORDERLYWEB_DOCUMENT.SHOW]).isEqualTo(1)

            Assertions.assertThat(result[3][Tables.ORDERLYWEB_DOCUMENT.PATH]).isEqualTo("/test/testdata/subdir/test.csv")
            Assertions.assertThat(result[3][Tables.ORDERLYWEB_DOCUMENT.NAME]).isEqualTo("test.csv")
            Assertions.assertThat(result[3][Tables.ORDERLYWEB_DOCUMENT.IS_FILE]).isEqualTo(1)
            Assertions.assertThat(result[3][Tables.ORDERLYWEB_DOCUMENT.PARENT]).isEqualTo("/test/testdata/subdir")
            Assertions.assertThat(result[3][Tables.ORDERLYWEB_DOCUMENT.SHOW]).isEqualTo(1)

            Assertions.assertThat(result[4][Tables.ORDERLYWEB_DOCUMENT.PATH]).isEqualTo("/test/testdata/test.doc")
            Assertions.assertThat(result[4][Tables.ORDERLYWEB_DOCUMENT.NAME]).isEqualTo("test.doc")
            Assertions.assertThat(result[4][Tables.ORDERLYWEB_DOCUMENT.IS_FILE]).isEqualTo(1)
            Assertions.assertThat(result[4][Tables.ORDERLYWEB_DOCUMENT.PARENT]).isEqualTo("/test/testdata")
            Assertions.assertThat(result[4][Tables.ORDERLYWEB_DOCUMENT.SHOW]).isEqualTo(1)

            Assertions.assertThat(result[5][Tables.ORDERLYWEB_DOCUMENT.PATH]).isEqualTo("/test/testdata/testUrl.web.url")
            Assertions.assertThat(result[5][Tables.ORDERLYWEB_DOCUMENT.NAME]).isEqualTo("https://github.com")
            Assertions.assertThat(result[5][Tables.ORDERLYWEB_DOCUMENT.IS_FILE]).isEqualTo(1)
            Assertions.assertThat(result[5][Tables.ORDERLYWEB_DOCUMENT.PARENT]).isEqualTo("/test/testdata")
            Assertions.assertThat(result[5][Tables.ORDERLYWEB_DOCUMENT.SHOW]).isEqualTo(1)

            Assertions.assertThat(result[6][Tables.ORDERLYWEB_DOCUMENT.PATH]).isEqualTo("/test/testdata/test_pdf.pdf")
            Assertions.assertThat(result[6][Tables.ORDERLYWEB_DOCUMENT.NAME]).isEqualTo("test_pdf.pdf")
            Assertions.assertThat(result[6][Tables.ORDERLYWEB_DOCUMENT.IS_FILE]).isEqualTo(1)
            Assertions.assertThat(result[6][Tables.ORDERLYWEB_DOCUMENT.PARENT]).isEqualTo("/test/testdata")
            Assertions.assertThat(result[6][Tables.ORDERLYWEB_DOCUMENT.SHOW]).isEqualTo(1)
        }
    }


    @Test
    fun `only document readers can get all documents`()
    {
        val url = "/documents/"
        assertWebUrlSecured(url, readDocuments, ContentTypes.json)
    }

    @Test
    fun `document readers can get all documents`()
    {
        JooqContext().use {
            insertDocument(it, "/path", 1, null)
        }
        val response = webRequestHelper.loginWithMontaguAndMakeRequest("/documents", readDocuments, ContentTypes.json)

        assertSuccessful(response)
        assertJsonContentType(response)
        val data = JSONValidator.getData(response.text)
        assertThat(data.isArray).isTrue()
        val doc = data[0]
        assertThat(doc.get("path").textValue()).isEqualTo("/path")

    }
}
