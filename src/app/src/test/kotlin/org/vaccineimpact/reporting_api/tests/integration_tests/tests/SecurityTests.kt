package org.vaccineimpact.reporting_api.tests.integration_tests.tests

import org.assertj.core.api.Assertions
import org.junit.Test
import org.vaccineimpact.reporting_api.tests.integration_tests.helpers.RequestHelper

class SecurityTests: IntegrationTest()
{

    @Test
    fun `returns 401 if token missing`(){

        val response = RequestHelper().getNoAuth("/reports")

        Assertions.assertThat(response.headers["content-type"]).isEqualTo("application/json")
        Assertions.assertThat(response.statusCode).isEqualTo(401)
        JSONValidator.validateError(response.text, "bearer-token-invalid",
                "Bearer token not supplied in Authorization header, or bearer token was invalid")

    }

    @Test
    fun `returns 401 if token not valid`(){

        val response = RequestHelper().getWrongAuth("/reports")

        Assertions.assertThat(response.headers["content-type"]).isEqualTo("application/json")
        Assertions.assertThat(response.statusCode).isEqualTo(401)
        JSONValidator.validateError(response.text, "bearer-token-invalid",
                "Bearer token not supplied in Authorization header, or bearer token was invalid")

    }

    @Test
    fun `returns 403 if missing permissions`(){

        val response = RequestHelper().getWrongPermissions("/reports")

        Assertions.assertThat(response.headers["content-type"]).isEqualTo("application/json")
        Assertions.assertThat(response.statusCode).isEqualTo(403)
        JSONValidator.validateError(response.text, "forbidden",
                "You do not have sufficient permissions to access this resource. Missing these permissions: */can-login")

    }

}