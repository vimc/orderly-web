package org.vaccineimpact.orderlyweb.tests.integration_tests.tests

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import freemarker.ext.beans.StringModel
import freemarker.template.SimpleHash
import freemarker.template.SimpleSequence
import freemarker.template.TemplateBooleanModel
import freemarker.template.TemplateModel
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.pac4j.core.profile.CommonProfile
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.Serializer
import org.vaccineimpact.orderlyweb.app_start.TemplateObjectWrapper
import org.vaccineimpact.orderlyweb.controllers.web.Serialise
import org.vaccineimpact.orderlyweb.models.*
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests
import org.vaccineimpact.orderlyweb.viewmodels.DocumentsViewModel
import org.vaccineimpact.orderlyweb.viewmodels.IndexViewModel
import java.time.Instant

class TemplateObjectWrapperTests : TeamcityTests()
{
    class TestClass(@Serialise("reportJson") val report: Report)

    @Test
    fun `can wrap complex properties`()
    {
        val now = Instant.now()
        val artFiles = listOf(FileInfo("graph.png", 1234))
        val report = ReportVersionDetails("r1",
                "first report",
                "v1",
                true,
                now,
                "a fake report",
                listOf(Artefact(ArtefactFormat.DATA, "a graph", artFiles)),
                listOf(),
                listOf(DataInfo("hash", 1234, 2345)),
                mapOf("param" to "value"))

        val sut = TemplateObjectWrapper()
        val result = sut.wrap(report) as SimpleHash

        assertThat(result["name"].toString()).isEqualTo("r1")
        assertThat(result["id"].toString()).isEqualTo("v1")
        assertThat((result["published"] as TemplateBooleanModel).asBoolean).isEqualTo(true)
        assertThat((result["date"]).toString()).isEqualTo(now.toString())

        val wrappedArtefacts = result["artefacts"] as SimpleSequence

        val wrappedArtefact = wrappedArtefacts[0] as StringModel
        assertThat(wrappedArtefact["format"].toString()).isEqualTo("data")
        assertThat(wrappedArtefact["description"].toString()).isEqualTo("a graph")

        val files = wrappedArtefact["files"] as SimpleSequence
        val fileModel = files[0] as StringModel
        assertThat(fileModel["name"].toString()).isEqualTo("graph.png")
        assertThat(fileModel["size"].toString()).isEqualTo("1234")

        val data = result["dataInfo"] as SimpleSequence
        val dataModel = data[0] as StringModel
        assertThat(dataModel["name"].toString()).isEqualTo("hash")
        assertThat(dataModel["csvSize"].toString()).isEqualTo("1234")
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
            on { hasPermission(any()) } doReturn true
        }
        val model = IndexViewModel(mockContext, listOf(), listOf(), listOf())

        val sut = TemplateObjectWrapper()
        val result = sut.wrap(model) as SimpleHash
        assertThat(result["appName"].toString()).isEqualTo("Reporting portal")
        assertThat(result["authProvider"].toString()).isEqualTo("Montagu")
        assertThat(result["appEmail"].toString()).isEqualTo("montagu-help@imperial.ac.uk")
        assertThat(result["user"].toString()).isEqualTo("user.name")
        assertThat((result["loggedIn"] as TemplateBooleanModel).asBoolean).isEqualTo(true)
        assertThat((result["isReviewer"] as TemplateBooleanModel).asBoolean).isEqualTo(true)
        assertThat((result["isAdmin"] as TemplateBooleanModel).asBoolean).isEqualTo(true)
        assertThat((result["fineGrainedAuth"] as TemplateBooleanModel).asBoolean).isEqualTo(true)
        assertThat((result["reports"])).isNotNull()
        assertThat((result["pinnedReports"])).isNotNull()
    }

    @Test
    fun `wraps DocumentsViewModel correctly`()
    {
        val mockContext = mock<ActionContext> {
            on { it.userProfile } doReturn CommonProfile().apply { id = "user.name" }
            on { hasPermission(any()) } doReturn true
        }
        val model = DocumentsViewModel.build(mockContext, listOf(Document("name", "path", true, false, listOf())))

        val sut = TemplateObjectWrapper()
        val result = sut.wrap(model) as SimpleHash
        val doc = ((result["docs"] as SimpleSequence)[0] as StringModel)
        assertThat((doc["file"] as TemplateBooleanModel).asBoolean).isEqualTo(true)
    }
}