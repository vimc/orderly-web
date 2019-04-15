package org.vaccineimpact.orderlyweb.tests.unit_tests

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import freemarker.ext.beans.StringModel
import freemarker.template.SimpleHash
import freemarker.template.TemplateBooleanModel
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.pac4j.core.profile.CommonProfile
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.Serializer
import org.vaccineimpact.orderlyweb.app_start.TemplateObjectWrapper
import org.vaccineimpact.orderlyweb.controllers.web.HomeController.IndexViewModel
import org.vaccineimpact.orderlyweb.controllers.web.Serialise
import org.vaccineimpact.orderlyweb.models.Report
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests

class TemplateObjectWrapperTests : TeamcityTests()
{
    class TestClass(@Serialise("reportJson") val report: Report)

    @Test
    fun `can wrap properties`()
    {
        val report = Report("some report","display name", "v1")
        val model = TestClass(report)
        val sut = TemplateObjectWrapper()
        val result = sut.wrap(model) as SimpleHash

        val wrappedReport = (result["report"] as StringModel)
        assertThat(wrappedReport["name"].toString()).isEqualTo("some report")
        assertThat(wrappedReport["displayName"].toString()).isEqualTo("display name")
        assertThat(wrappedReport["latestVersion"].toString()).isEqualTo("v1")
    }

    @Test
    fun `adds extra field when marked with the serialise attribute`()
    {
        val report = Report("some report", "display name", "v1")
        val model = TestClass(report)
        val sut = TemplateObjectWrapper()
        val result = sut.wrap(model) as SimpleHash

        assertThat(result["reportJson"].toString())
                .isEqualTo(Serializer.instance.gson.toJson(report))
    }

    @Test
    fun `includes all declared and inherited properties of view model`()
    {
        val mockContext = mock<ActionContext> {
            on { it.userProfile } doReturn CommonProfile().apply { id = "user.name" }
        }
        val model = IndexViewModel(mockContext, listOf())

        val sut = TemplateObjectWrapper()
        val result = sut.wrap(model) as SimpleHash
        assertThat(result["appName"].toString()).isEqualTo("Reporting portal")
        assertThat(result["authProvider"].toString()).isEqualTo("montagu")
        assertThat(result["appEmail"].toString()).isEqualTo("montagu-help@imperial.ac.uk")
        assertThat(result["user"].toString()).isEqualTo("user.name")
        assertThat((result["loggedIn"] as TemplateBooleanModel).asBoolean).isEqualTo(true)
    }
}