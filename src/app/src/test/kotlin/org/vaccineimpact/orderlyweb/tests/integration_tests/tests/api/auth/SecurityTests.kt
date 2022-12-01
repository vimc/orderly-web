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

}