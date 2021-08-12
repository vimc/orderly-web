package org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.web

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.assertj.core.api.Assertions
import org.junit.Test
import spark.Response
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.web.SecurityController
import org.vaccineimpact.orderlyweb.db.Config
import org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.api.ControllerTest
import org.vaccineimpact.orderlyweb.viewmodels.IndexViewModel

class SecurityControllerTests : ControllerTest()
{
    @Test
    fun `returns expected model on web login`()
    {
        val mockContext = mock<ActionContext> {
            on { this.queryParams("requestedUrl") } doReturn "testUrl"
        }

        val sut = SecurityController(mockContext)

        val result = sut.weblogin()
        Assertions.assertThat(result.requestedUrl).isEqualTo("testUrl")
        Assertions.assertThat(result.breadcrumbs.count()).isEqualTo(2)
        Assertions.assertThat(result.breadcrumbs.first()).isEqualTo(IndexViewModel.breadcrumb)
        Assertions.assertThat(result.breadcrumbs.last().url).isEqualTo("/weblogin")
        Assertions.assertThat(result.breadcrumbs.last().name).isEqualTo("Login")
    }

    @Test
    fun `HTML encodes requested URL for view model`()
    {
        val mockContext = mock<ActionContext> {
            on { this.queryParams("requestedUrl") } doReturn "<testUrl>"
        }

        val sut = SecurityController(mockContext)

        val result = sut.weblogin()
        Assertions.assertThat(result.requestedUrl).isEqualTo("&lt;testUrl&gt;")
    }

    @Test
    fun `defaults to homepage as requestedUrl when no url is explicitly requested`()
    {
        val sut = SecurityController(mock())

        val result = sut.weblogin()
        Assertions.assertThat(result.requestedUrl).isEqualTo("/")
    }

    @Test
    fun `redirects to requested url on web login external`()
    {
        val mockResponse = mock<Response>{}
        val mockContext = mock<ActionContext> {
            on { this.queryParams("requestedUrl") } doReturn "testUrl"
            on { this.getSparkResponse()} doReturn mockResponse
        }

        val sut = SecurityController(mockContext)

        sut.webloginExternal()
        verify(mockResponse).redirect("testUrl")
    }
}
