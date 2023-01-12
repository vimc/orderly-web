package org.vaccineimpact.orderlyweb.tests.integration_tests.tests.api

import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.vaccineimpact.orderlyweb.tests.integration_tests.helpers.fakeGlobalReportReader
import org.vaccineimpact.orderlyweb.tests.integration_tests.tests.IntegrationTest

class OutpackTests: IntegrationTest()
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
}
