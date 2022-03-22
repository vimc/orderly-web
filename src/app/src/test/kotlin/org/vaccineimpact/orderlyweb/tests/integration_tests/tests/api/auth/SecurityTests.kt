package org.vaccineimpact.orderlyweb.tests.integration_tests.tests.api.auth

import org.assertj.core.api.Assertions
import org.junit.Test
import org.vaccineimpact.orderlyweb.ContentTypes
import org.vaccineimpact.orderlyweb.security.WebTokenHelper
import org.vaccineimpact.orderlyweb.test_helpers.insertReport
import org.vaccineimpact.orderlyweb.tests.integration_tests.helpers.APIRequestHelper
import org.vaccineimpact.orderlyweb.tests.integration_tests.helpers.fakeGlobalReportReviewer
import org.vaccineimpact.orderlyweb.tests.integration_tests.helpers.fakeReportReader
import org.vaccineimpact.orderlyweb.tests.integration_tests.tests.IntegrationTest
import java.time.Instant
import java.util.*

class SecurityTests : IntegrationTest()
{

    /**  Bearer token tests */

    @Test
    fun `returns 401 if token missing`()
    {

        val response = APIRequestHelper().getNoAuth("/reports/")

        assertJsonContentType(response)
        Assertions.assertThat(response.statusCode).isEqualTo(401)
        JSONValidator.validateError(response.text, "bearer-token-invalid",
                "Bearer token not supplied in Authorization header, or bearer token was invalid")
    }

    @Test
    fun `returns 401 if token not valid`()
    {

        val response = APIRequestHelper().getWrongAuth("/reports/")

        assertJsonContentType(response)
        Assertions.assertThat(response.statusCode).isEqualTo(401)
        JSONValidator.validateError(response.text, "bearer-token-invalid",
                "Bearer token not supplied in Authorization header, or bearer token was invalid")
    }

    @Test
    fun `returns 403 if missing permissions`()
    {
        val response = APIRequestHelper().getWrongPermissions("/reports/")

        assertJsonContentType(response)
        Assertions.assertThat(response.statusCode).isEqualTo(403)
        JSONValidator.validateError(response.text, "forbidden",
                "You do not have sufficient permissions to access this resource. Missing these permissions: */reports.read")
    }

    @Test
    fun `returns 401 if bearer token is expired`()
    {
        val alreadyExpired = Date.from(Instant.now())
        val claims = WebTokenHelper.instance.issuer.bearerTokenClaims(fakeGlobalReportReviewer())
        val expiredClaims = claims.filter { it.component1() != "exp" } + mapOf("exp" to alreadyExpired)
        val expiredToken = WebTokenHelper.instance.issuer.generator.generate(expiredClaims)

        val response = apiRequestHelper.getWithToken("/reports/", expiredToken)

        assertJsonContentType(response)
        Assertions.assertThat(response.statusCode).isEqualTo(401)
        JSONValidator.validateError(response.text, "bearer-token-invalid",
                "Token has expired. Please request a new one.")
    }

    /**  Access token tests */

    @Test
    fun `returns 403 if missing permissions with access token`()
    {
        val url = "/reports/testname/versions/testversion/artefacts/someartefact/"
        val token = apiRequestHelper.generateOnetimeToken(url, fakeReportReader("wrongreport"))

        val response = apiRequestHelper
                .getNoAuth("/reports/testname/versions/testversion/artefacts/someartefact/?access_token=$token",
                        ContentTypes.binarydata)

        assertJsonContentType(response)
        Assertions.assertThat(response.statusCode).isEqualTo(403)
        JSONValidator.validateError(response.text, "forbidden",
                "You do not have sufficient permissions to access this resource." +
                        " Missing these permissions: report:testname/reports.read")
    }

    @Test
    fun `returns 403 if access token url is wrong`()
    {
        insertReport("testname", "testversion")

        val token = apiRequestHelper
                .generateOnetimeToken("/reports/testname/versions/testversion/artefacts/someartefact/")
        val response = apiRequestHelper
                .getNoAuth("/reports/testname/versions/testversion/artefacts/someotherartefact/?access_token=$token",
                        ContentTypes.binarydata)

        assertJsonContentType(response)
        Assertions.assertThat(response.statusCode).isEqualTo(403)
        JSONValidator.validateError(response.text, "forbidden",
                "This token is issued for /api/v1/reports/testname/versions/testversion/artefacts/someartefact/ but the " +
                        "current request is for /api/v1/reports/testname/versions/testversion/artefacts/someotherartefact/")
    }

    @Test
    fun `return 401 if invalid access token`()
    {
        insertReport("testname", "testversion")
        val response = apiRequestHelper
                .getNoAuth("/reports/testname/versions/testversion/artefacts/fakeartefact/?access_token=42678iwek",
                        ContentTypes.binarydata)

        assertJsonContentType(response)
        Assertions.assertThat(response.statusCode).isEqualTo(401)
        JSONValidator.validateMultipleAuthErrors(response.text)
    }

    @Test
    fun `returns 401 if access token not in db`()
    {
        insertReport("testname", "testversion")
        val url = "/reports/testname/versions/testversion/artefacts/6943yhks/"
        val token = WebTokenHelper.instance.issuer
                .generateOnetimeActionToken(fakeGlobalReportReviewer(), url)
        val response = apiRequestHelper
                .getNoAuth("$url?access_token=$token", ContentTypes.binarydata)

        assertJsonContentType(response)
        Assertions.assertThat(response.statusCode).isEqualTo(401)
        JSONValidator.validateMultipleAuthErrors(response.text)
    }

    @Test
    fun `returns 401 if missing access token and no auth`()
    {
        insertReport("testname", "testversion")
        val fakeartefact = "hf647rhj"

        val url = "/reports/testname/versions/testversion/artefacts/$fakeartefact"
        val response = apiRequestHelper.getNoAuth("$url/", ContentTypes.binarydata)

        assertJsonContentType(response)
        Assertions.assertThat(response.statusCode).isEqualTo(401)
        JSONValidator.validateMultipleAuthErrors(response.text)
    }
}
