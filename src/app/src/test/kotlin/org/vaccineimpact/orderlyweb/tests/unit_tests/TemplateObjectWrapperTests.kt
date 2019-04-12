package org.vaccineimpact.orderlyweb.tests.unit_tests

import freemarker.template.SimpleHash
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.vaccineimpact.orderlyweb.Serializer
import org.vaccineimpact.orderlyweb.app_start.TemplateObjectWrapper
import org.vaccineimpact.orderlyweb.controllers.web.Serialise
import org.vaccineimpact.orderlyweb.models.Report
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests

class TemplateObjectWrapperTests : TeamcityTests()
{
    class TestClass(@Serialise("reportJson") val report: Report)

    @Test
    fun `adds extra fields`()
    {
        val report = Report("report", "display name", "v1")
        val model = TestClass(report)
        val sut = TemplateObjectWrapper()
        val result = sut.wrap(model) as SimpleHash
        assertThat(result["reportJson"].toString())
                .isEqualTo(Serializer.instance.gson.toJson(report))
    }
}