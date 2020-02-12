package org.vaccineimpact.orderlyweb.tests.integration_tests.tests.web

import org.assertj.core.api.Assertions
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.vaccineimpact.orderlyweb.ContentTypes
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.tests.integration_tests.tests.IntegrationTest
import java.io.File

class DocumentTests : IntegrationTest()
{
    private val readDocuments = setOf(ReifiedPermission("documents.read", Scope.Global()))

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
}
