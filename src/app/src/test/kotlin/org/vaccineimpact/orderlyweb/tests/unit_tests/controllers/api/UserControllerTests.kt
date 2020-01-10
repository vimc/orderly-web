package org.vaccineimpact.orderlyweb.tests.unit_tests.controllers.api

import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.verify
import org.assertj.core.api.Assertions
import org.junit.Test
import org.vaccineimpact.orderlyweb.ActionContext
import org.vaccineimpact.orderlyweb.controllers.api.UserController
import org.vaccineimpact.orderlyweb.db.UserRepository
import org.vaccineimpact.orderlyweb.errors.MissingParameterError
import org.vaccineimpact.orderlyweb.models.UserSource
import org.vaccineimpact.orderlyweb.security.WebTokenHelper

class UserControllerTests
{
    @Test
    fun `adds user`()
    {
        val actionContext = mock<ActionContext> {
            on { this.postData() } doReturn mapOf(
                    "email" to "test@test.com",
                    "username" to "firstname.lastname",
                    "displayName" to "Firstname Lastname",
                    "source" to "Montagu"
            )
        }
        val mockRepo = mock<UserRepository>()

        val sut = UserController(actionContext, WebTokenHelper.instance, mockRepo)
        sut.addUser()
        verify(mockRepo).addUser("test@test.com", "firstname.lastname", "Firstname Lastname",
                                    UserSource.Montagu)
    }

    @Test
    fun `addUser throws MissingParameter exception if any param is missing`()
    {
        assertErrorOnMissingParam("email")
        assertErrorOnMissingParam("username")
        assertErrorOnMissingParam("displayName")
        assertErrorOnMissingParam("source")
    }

    private fun assertErrorOnMissingParam(missing: String) {
        val postData = mapOf(
                "email" to "test@test.com",
                "username" to "firstname.lastname",
                "displayName" to "Firstname Lastname",
                "source" to "Montagu"
        ).toMutableMap()
        postData.remove(missing)

        val actionContext = mock<ActionContext> {
            on { this.postData() } doReturn postData
        }
        val sut = UserController(actionContext, WebTokenHelper.instance, mock())
        Assertions.assertThatThrownBy { sut.addUser() }.isInstanceOf(MissingParameterError::class.java)
    }
}