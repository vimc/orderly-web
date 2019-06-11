package org.vaccineimpact.orderlyweb.tests.integration_tests.tests

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import freemarker.ext.beans.StringModel
import freemarker.template.SimpleHash
import freemarker.template.SimpleSequence
import freemarker.template.TemplateBooleanModel
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.pac4j.core.profile.CommonProfile
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.Serializer
import org.vaccineimpact.orderlyweb.app_start.TemplateObjectWrapper
import org.vaccineimpact.orderlyweb.viewmodels.IndexViewModel
import org.vaccineimpact.orderlyweb.controllers.web.Serialise
import org.vaccineimpact.orderlyweb.models.Artefact
import org.vaccineimpact.orderlyweb.models.ArtefactFormat
import org.vaccineimpact.orderlyweb.models.Report
import org.vaccineimpact.orderlyweb.models.ReportVersionDetails
import org.vaccineimpact.orderlyweb.test_helpers.TeamcityTests
import java.time.Instant

class TemplateObjectWrapperTests : TeamcityTests()
{
    class TestClass(@Serialise("reportJson") val report: Report)

    @Test
    fun `can wrap complex properties`()
    {
        val now = Instant.now()
        val report = ReportVersionDetails("r1",
                "first report",
                "v1",
                true,
                now,
                "dr author",
                "ms funder",
                "a fake report",
                listOf(Artefact(ArtefactFormat.DATA, "a graph", listOf("graph.png"))),
                listOf(),
                mapOf("hash" to "data.csv"))

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
        assertThat(files[0].toString()).isEqualTo("graph.png")

        val data = result["dataHashes"] as SimpleHash
        assertThat(data["hash"].toString()).isEqualTo("data.csv")
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
        val model = IndexViewModel(mockContext, listOf(), listOf(), true)

        val sut = TemplateObjectWrapper()
        val result = sut.wrap(model) as SimpleHash
        assertThat(result["appName"].toString()).isEqualTo("Reporting portal")
        assertThat(result["authProvider"].toString()).isEqualTo("Montagu")
        assertThat(result["appEmail"].toString()).isEqualTo("montagu-help@imperial.ac.uk")
        assertThat(result["user"].toString()).isEqualTo("user.name")
        assertThat((result["loggedIn"] as TemplateBooleanModel).asBoolean).isEqualTo(true)
        assertThat((result["isReviewer"] as TemplateBooleanModel).asBoolean).isEqualTo(true)
        assertThat((result["reports"])).isNotNull()
        assertThat((result["pinnedReports"])).isNotNull()
    }
}