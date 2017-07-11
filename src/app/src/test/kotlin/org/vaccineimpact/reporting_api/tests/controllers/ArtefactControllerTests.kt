package org.vaccineimpact.reporting_api.tests.controllers

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.json.JSONObject
import org.vaccineimpact.reporting_api.ActionContext
import org.vaccineimpact.reporting_api.OrderlyClient
import org.vaccineimpact.reporting_api.controllers.ArtefactController

class ArtefactControllerTests{

    @Test
    fun `gets artefacts for report`() {
        val name = "testname"
        val version = "testversion"

        val artefacts = JSONObject("{\"test.png\":{\"format\":\"staticgraph\",\"description\":\"A plot of coverage over time\"}}")

        val orderly = mock<OrderlyClient> {
            on { this.getArtefacts(name, version) } doReturn artefacts
        }

        val actionContext = mock<ActionContext> {
            on {this.params(":name")} doReturn name
            on {this.params(":version")} doReturn version
        }

        val sut = ArtefactController(orderly)

        assertThat(sut.get(actionContext)).isEqualTo(artefacts)
    }

}
