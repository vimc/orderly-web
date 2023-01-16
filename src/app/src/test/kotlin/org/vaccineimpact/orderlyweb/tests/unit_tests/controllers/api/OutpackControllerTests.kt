import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import okhttp3.Headers
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.PorcelainAPI
import org.vaccineimpact.orderlyweb.PorcelainResponse
import org.vaccineimpact.orderlyweb.controllers.api.OutpackController
import org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.api.ControllerTest

class ArtefactControllerTests : ControllerTest()
{

    @Test
    fun `gets correct route from outpack with no splat`()
    {
        val mockContext = mock<ActionContext>()
        val mockOutpack = mock<PorcelainAPI> {
            on { it.get("/", mockContext) } doReturn PorcelainResponse("index", 200, Headers.headersOf())
        }

        val sut = OutpackController(mockContext, mockOutpack)
        val response = sut.get()

        assertThat(response).isEqualTo("index")
    }

    @Test
    fun `gets correct route from outpack with splat`()
    {
        val mockContext = mock<ActionContext>() {
            on { it.splat() } doReturn arrayOf("some/route")
        }
        val mockOutpack = mock<PorcelainAPI> {
            on { it.get("/some/route", mockContext) } doReturn PorcelainResponse("route", 200, Headers.headersOf())
        }

        val sut = OutpackController(mockContext, mockOutpack)
        val response = sut.get()

        assertThat(response).isEqualTo("route")
    }
}