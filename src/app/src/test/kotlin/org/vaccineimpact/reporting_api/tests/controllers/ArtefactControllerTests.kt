package org.vaccineimpact.reporting_api.tests.controllers

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.vaccineimpact.reporting_api.ActionContext
import org.vaccineimpact.reporting_api.Artefact
import org.vaccineimpact.reporting_api.FileSystem
import org.vaccineimpact.reporting_api.OrderlyClient
import org.vaccineimpact.reporting_api.controllers.ArtefactController
import org.vaccineimpact.reporting_api.errors.UnknownObjectError
import org.vaccineimpact.reporting_api.test_helpers.MontaguTests

class ArtefactControllerTests : MontaguTests()
{

    @Test
    fun `gets artefacts for report`() {
        val name = "testname"
        val version = "testversion"

        val artefacts = mutableMapOf<String, String>()
        artefacts.put("test.png", "hjkdasjkldas6762i1j")

        val orderly = mock<OrderlyClient> {
            on { this.getArtefacts(name, version) } doReturn artefacts
        }

        val actionContext = mock<ActionContext> {
            on {this.params(":name")} doReturn name
            on {this.params(":version")} doReturn version
        }

        val sut = ArtefactController(orderly, mock<FileSystem>())

        assertThat(sut.get(actionContext)).isEqualTo(artefacts)
    }

    @Test
    fun `throws unknown object error if artefact does not exist for report`() {
        val name = "testname"
        val version = "testversion"
        val artefact = "test.png"

        val orderly = mock<OrderlyClient> {
            on { this.hasArtefact(name, version, artefact) } doReturn false
        }

        val actionContext = mock<ActionContext> {
            on {this.params(":name")} doReturn name
            on {this.params(":version")} doReturn version
            on {this.params(":artefact")} doReturn artefact
        }

        val sut = ArtefactController(orderly, mock<FileSystem>())

        assertThatThrownBy { sut.download(actionContext) }
                .isInstanceOf(UnknownObjectError::class.java)
    }

}
