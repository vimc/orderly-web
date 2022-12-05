package org.vaccineimpact.orderlyweb.tests.integration_tests.tests.web

import org.assertj.core.api.Java6Assertions.assertThat
import org.junit.jupiter.api.Test
import org.vaccineimpact.orderlyweb.ContentTypes
import org.vaccineimpact.orderlyweb.tests.integration_tests.tests.IntegrationTest

class MetricsTests : IntegrationTest()
{
    @Test
    fun `can get metrics`()
    {
        val response = webRequestHelper.getWebPage("/metrics", ContentTypes.text)
        assertThat(response.statusCode).isEqualTo(200)

        assertThat(response.text).isEqualTo("running 1")
        assertPlainTextContentType(response)
    }
}
