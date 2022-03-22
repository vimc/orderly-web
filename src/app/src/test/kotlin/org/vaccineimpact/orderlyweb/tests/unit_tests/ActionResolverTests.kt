package org.vaccineimpact.orderlyweb.tests.unit_tests

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test
import org.vaccineimpact.orderlyweb.APIEndpoint
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.app_start.ActionResolver
import org.vaccineimpact.orderlyweb.controllers.Controller
import org.vaccineimpact.orderlyweb.controllers.web.Template
import spark.TemplateEngine

class ActionResolverTests
{
    private val mockTemplateEngine = mock<TemplateEngine> {
        on { render(any()) } doReturn "rendered"
    }

    class FakeController(actionContext: ActionContext) : Controller(actionContext)
    {
        @Suppress("FunctionOnlyReturningConstant")
        fun simpleAction(): Int
        {
            return 1234
        }

        @Template("whatever")
        @Suppress("FunctionOnlyReturningConstant")
        fun templateAction(): Int
        {
            return 1234
        }
    }

    @Test
    fun `returns simple object where action does not have template attribute`()
    {
        val sut = ActionResolver(mockTemplateEngine)
        val endpoint = APIEndpoint("/url", FakeController::class, "simpleAction")
        val result = sut.invokeControllerAction(endpoint, mock())
        assertThat(result).isEqualTo(1234)
    }

    @Test
    fun `returns model and view rendered to string where action has template attribute`()
    {
        val sut = ActionResolver(mockTemplateEngine)
        val endpoint = APIEndpoint("/url", FakeController::class, "templateAction")
        val result = sut.invokeControllerAction(endpoint, mock())
        assertThat(result).isEqualTo("rendered")
    }

    @Test
    fun `throws NoSuchMethod exception if method does not exist`()
    {
        val sut = ActionResolver(mockTemplateEngine)
        val endpoint = APIEndpoint("/url", FakeController::class, "nonexistentAction")
        assertThatThrownBy { sut.invokeControllerAction(endpoint, mock()) }
                .isInstanceOf(NoSuchMethodException::class.java)
    }
}
