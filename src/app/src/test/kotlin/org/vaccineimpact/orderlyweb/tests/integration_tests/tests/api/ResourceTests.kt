package org.vaccineimpact.orderlyweb.tests.integration_tests.tests.api

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.vaccineimpact.orderlyweb.models.FilePurpose
import org.vaccineimpact.orderlyweb.ContentTypes
import org.vaccineimpact.orderlyweb.db.AppConfig
import org.vaccineimpact.orderlyweb.models.Scope
import org.vaccineimpact.orderlyweb.models.permissions.ReifiedPermission
import org.vaccineimpact.orderlyweb.tests.insertFileInput
import org.vaccineimpact.orderlyweb.test_helpers.insertReport
import org.vaccineimpact.orderlyweb.tests.integration_tests.helpers.fakeGlobalReportReviewer
import org.vaccineimpact.orderlyweb.tests.integration_tests.helpers.fakeReportReader
import org.vaccineimpact.orderlyweb.tests.integration_tests.tests.IntegrationTest
import java.io.File

class ResourceTests : IntegrationTest()
{
    @Test
    fun `gets dict of resource names to hashes with scoped report reading permission`()
    {
        insertReport("testname", "testversion")
        val response = apiRequestHelper.get(
                "/reports/testname/versions/testversion/resources",
                userEmail = fakeReportReader("testname")
        )

        assertJsonContentType(response)
    }

    @Test
    fun `only report readers can get dict of resource names to hashes`()
    {
        insertReport("testname", "testversion")
        assertAPIUrlSecured(
                "/reports/testname/versions/testversion/resources",
                setOf(ReifiedPermission("reports.read", Scope.Specific("report", "testname"))),
                contentType = ContentTypes.json
        )
    }

    @Test
    fun `gets resource file with space in name`()
    {
        val version = File("${AppConfig()["orderly.root"]}/archive/spaces/").list()[0]

        val resourceEncoded = "a+resource+with+spaces.csv"
        val url = "/reports/spaces/versions/$version/resources/$resourceEncoded/"

        val response = apiRequestHelper.get(url, ContentTypes.binarydata, userEmail = fakeGlobalReportReviewer())

        assertSuccessful(response)
        Assertions.assertThat(response.headers["content-type"]).isEqualTo("application/octet-stream")
        Assertions.assertThat(response.headers["content-disposition"]).isEqualTo("attachment; filename=\"spaces/$version/a resource with spaces.csv\"")
    }

    @Test
    fun `gets resource file with quotes in name`()
    {
        val version = File("${AppConfig()["orderly.root"]}/archive/spaces/").list()[0]

        val url = "/reports/spaces/versions/$version/resources/some%22data%22.csv"
        val response = apiRequestHelper.get(url, ContentTypes.binarydata, userEmail = fakeGlobalReportReviewer())

        assertSuccessful(response)
        Assertions.assertThat(response.headers["content-type"]).isEqualTo("application/octet-stream")
        Assertions.assertThat(response.headers["content-disposition"]).isEqualTo("attachment; filename=\"spaces/$version/some\"data\".csv\"")
    }

    @Test
    fun `only report readers can get resource file`()
    {
        val version = File("${AppConfig()["orderly.root"]}/archive/use_resource/").list()[0]
        val resourceEncoded = "meta:data.csv"
        val url = "/reports/use_resource/versions/$version/resources/$resourceEncoded/"

        assertAPIUrlSecured(
                url,
                setOf(ReifiedPermission("reports.read", Scope.Specific("report", "use_resource")))
        )
    }

    @Test
    fun `gets 404 if resource doesnt exist in db`()
    {
        insertReport("testname", "testversion")
        val fakeresource = "hf647rhj"
        val url = "/reports/testname/versions/testversion/resources/$fakeresource/"
        val response = apiRequestHelper.get(url, ContentTypes.binarydata)

        assertJsonContentType(response)
        Assertions.assertThat(response.statusCode).isEqualTo(404)
        JSONValidator.validateError(response.text, "unknown-resource", "Unknown resource : '$fakeresource'")
    }

    @Test
    fun `gets 404 if resource file doesnt exist`()
    {
        insertReport("testname", "testversion")
        insertFileInput("testversion", "resource.csv", FilePurpose.RESOURCE)

        val url = "/reports/testname/versions/testversion/resources/resource.csv/"
        val response = apiRequestHelper.get(url, ContentTypes.binarydata)

        assertJsonContentType(response)
        Assertions.assertThat(response.statusCode).isEqualTo(404)
        JSONValidator.validateError(response.text, "file-not-found", "File with name 'resource.csv' does not exist")
    }

}
