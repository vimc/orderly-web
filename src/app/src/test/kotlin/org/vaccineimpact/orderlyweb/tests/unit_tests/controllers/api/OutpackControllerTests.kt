import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.OutpackServerClient
import org.vaccineimpact.orderlyweb.PorcelainAPI
import org.vaccineimpact.orderlyweb.PorcelainResponse
import org.vaccineimpact.orderlyweb.controllers.api.OutpackController
import org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.api.ControllerTest

class ArtefactControllerTests : ControllerTest() {

    private val mockContext = mock<ActionContext>()
    private val mockResponse = PorcelainResponse("testResponse", 200)

    @Test
    fun `fetch index from outpack`()
    {
        val mockOutpack = mock<PorcelainAPI> {
            on { it.get("/", mockContext) } doReturn mockResponse
        }

        val sut = OutpackController(mockContext, mockOutpack)
        val response = sut.index()

        assertThat(response).isEqualTo("testResponse")
    }
}